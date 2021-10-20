package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*
import java.nio.file.Path

/**
 * A mutable builder for the build tree structure.
 */
class DefaultBuildTreeBuilder(
    private val rootDir: Path, private val pluginSpecFactory: PluginSpecFactory, private val librarySpecFactory: LibrarySpecFactory
) : BuildTreeBuilder {
    private val builds = mutableListOf<BuildBuilderImpl>()
    private val main = BuildBuilderImpl(null, "main build", BaseName("main"), "main", rootDir)

    override val mainBuild: BuildBuilder
        get() = main

    init {
        builds.add(main)
    }

    override var includeConfigurationCacheProblems = false

    override fun <T> mainBuild(body: BuildBuilder.() -> T): T {
        return body(mainBuild)
    }

    fun build(): BuildTreeSpec {
        val mapper = Mapper<BuildSpec>()
        return BuildTreeSpecImpl(rootDir, mapper.map(builds))
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path, override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private class PluginRefImpl(
        val plugin: PluginUseSpec
    ) : PluginRef

    private abstract class LazyValue<T : Any> {
        abstract fun get(): T
    }

    private class FixedValue<T : Any>(private val value: T) : LazyValue<T>() {
        override fun get(): T {
            return value
        }
    }

    private class DeferredLazyValue<T : Any> : LazyValue<T>() {
        private lateinit var value: T

        fun set(value: T) {
            this.value = value
        }

        override fun get(): T {
            return value
        }
    }

    private class CollectionLazyValue<T : Any> : LazyValue<List<T>>() {
        private val items = mutableListOf<T>()
        private var finalized = false

        fun add(item: T) {
            require(!finalized)
            items.add(item)
        }

        fun finished() {
            finalized = true
        }

        override fun get(): List<T> {
            require(finalized)
            return items
        }
    }

    private interface Mappable<T> {
        fun toSpec(mapper: Mapper<T>): T
    }

    private class Mapper<T> {
        private val mappedItems = mutableMapOf<Mappable<T>, T>()
        private val visiting = mutableSetOf<Mappable<T>>()

        fun map(items: List<Mappable<T>>): List<T> = items.map { map(it) }

        fun map(item: Mappable<T>): T {
            val result = mappedItems[item]
            if (result != null) {
                return result
            }
            if (!visiting.add(item)) {
                throw IllegalStateException("Cycle in object graph.")
            }
            try {
                val mapped = item.toSpec(this)
                mappedItems[item] = mapped
                return mapped
            } finally {
                visiting.remove(item)
            }
        }
    }

    private class MappingLazyValue<T : Any, S : Mappable<T>>(
        val mapper: Mapper<T>
    ) : LazyValue<List<T>>() {
        private val specs = mutableListOf<S>()
        private lateinit var items: List<T>
        private var finalized = false

        fun add(item: S) {
            require(!finalized)
            specs.add(item)
        }

        fun finished() {
            finalized = true
            items = mapper.map(specs)
        }

        override fun get(): List<T> {
            require(finalized)
            return items
        }
    }

    private class ExportedLibrarySpec(
        val coordinates: ExternalLibraryCoordinates,
        val api: LibraryProductionSpec,
        val usesPlugins: LazyValue<List<PluginUseSpec>>,
        val requiresLibrariesFromThisBuild: List<ExportedLibrarySpec>,
        val implementationLibraries: LazyValue<List<InternalLibraryProductionSpec>>,
        val incomingLibraries: LazyValue<List<ExternalLibraryUseSpec>>
    ) : LibraryRef, Mappable<ExternalLibraryProductionSpec> {
        val useSpec = ExternalLibraryUseSpec(coordinates, api.toApiSpec())

        override fun toSpec(mapper: Mapper<ExternalLibraryProductionSpec>) =
            ExternalLibraryProductionSpec(coordinates, api, usesPlugins.get(), incomingLibraries.get(), mapper.map(requiresLibrariesFromThisBuild), implementationLibraries.get())
    }

    private class InternalLibraryImpl(
        val baseName: BaseName,
        val spec: LibraryProductionSpec,
        val usesPlugins: LazyValue<List<PluginUseSpec>>
    ) : Mappable<InternalLibraryProductionSpec> {
        override fun toSpec(mapper: Mapper<InternalLibraryProductionSpec>): InternalLibraryProductionSpec {
            return InternalLibraryProductionSpec(baseName, spec, usesPlugins.get())
        }
    }

    private class LibrariesRefImpl(
        override val top: ExportedLibrarySpec, override val bottom: ExportedLibrarySpec
    ) : LibrariesRef

    private class AppImpl(
        val baseName: BaseName,
        val usesPlugins: LazyValue<List<PluginUseSpec>>,
        val implementationLibraries: LazyValue<List<InternalLibraryProductionSpec>>,
        val incomingLibraries: LazyValue<List<ExternalLibraryUseSpec>>
    ) : Mappable<AppProductionSpec> {
        override fun toSpec(mapper: Mapper<AppProductionSpec>) = AppProductionSpec(baseName, usesPlugins.get(), incomingLibraries.get(), implementationLibraries.get())
    }

    private inner class BuildBuilderImpl(
        val owner: BuildBuilderImpl?, val displayName: String, val baseName: BaseName, val artifactType: String, val rootDir: Path
    ) : BuildBuilder, Mappable<BuildSpec> {
        private val children = mutableListOf<BuildBuilderImpl>()
        private var pluginBuilds = 0
        private var hasInternalLibrary = false
        private val appsMapper = Mapper<AppProductionSpec>()
        private val libsMapper = Mapper<ExternalLibraryProductionSpec>()
        private val internalLibsMapper = Mapper<InternalLibraryProductionSpec>()
        private val internalComponents = ComponentsBuilder(appsMapper, libsMapper, internalLibsMapper)
        private val producesPlugins = CollectionLazyValue<PluginProductionSpec>()
        private val usesPlugins = CollectionLazyValue<PluginUseSpec>()
        private val usesLibraries = CollectionLazyValue<ExternalLibraryUseSpec>()
        private val exportedComponents = ComponentsBuilder(appsMapper, libsMapper, internalLibsMapper)
        private var projectNames: NameProvider = FixedNames(emptyList(), baseName.camelCase)
        private var includeSelf = false

        override fun toString(): String {
            return displayName
        }

        override fun pluginBuild(name: String): BuildBuilder {
            val build = addBuild(name, pluginBuilds)
            pluginBuilds++
            return build
        }

        override fun <T> pluginBuild(name: String, body: BuildBuilder.() -> T): T {
            return body(pluginBuild(name))
        }

        override fun build(name: String): BuildBuilder {
            return addBuild(name, children.size)
        }

        override fun <T> build(name: String, body: BuildBuilder.() -> T): T {
            return body(build(name))
        }

        private fun addBuild(name: String, index: Int): BuildBuilderImpl {
            require(!name.contains(':') && !name.contains('/'))
            require(!children.any { it.baseName.camelCase == BaseName(name).camelCase })
            val build = BuildBuilderImpl(this, "build $name", BaseName(name), name, rootDir.resolve(name))
            children.add(index, build)
            builds.add(build)
            return build
        }

        override fun producesPlugin(): PluginRef {
            val plugin = pluginSpecFactory.plugin(baseName, artifactType, "test.${baseName.lowerCaseDotSeparator}")
            producesPlugins.add(plugin)
            return PluginRefImpl(plugin.toUseSpec())
        }

        override fun producesApp() {
            exportedComponents.producesApps.add(AppImpl(BaseName(projectNames.next()), usesPlugins, implementationLibs(), usesLibraries))
        }

        private fun implementationLibs(): LazyValue<List<InternalLibraryProductionSpec>> {
            if (!hasInternalLibrary) {
                val libraryName = BaseName(projectNames.next())
                val spec = librarySpecFactory.maybeLibrary(libraryName.camelCase)
                if (spec != null) {
                    internalComponents.implementationLibraries.add(InternalLibraryImpl(libraryName, spec, usesPlugins))
                }
                hasInternalLibrary = true
            }
            return internalComponents.implementationLibraries
        }

        override fun includeSelf() {
            includeSelf = true
        }

        override fun producesLibrary(): LibraryRef {
            return addLibrary(true, implementationLibs())
        }

        override fun producesLibraries(): LibrariesRef {
            val bottom = addLibrary(false, implementationLibs())
            val top = addLibrary(true, FixedValue(emptyList()), listOf(bottom))
            return LibrariesRefImpl(top, bottom)
        }

        private fun addLibrary(useIncomingLibraries: Boolean, implementationLibs: LazyValue<List<InternalLibraryProductionSpec>>, requiresLibrariesFromThisBuild: List<ExportedLibrarySpec> = emptyList()): ExportedLibrarySpec {
            val coordinates = ExternalLibraryCoordinates("test.${baseName.lowerCaseDotSeparator}", projectNames.next(), "1.0")
            val libraryApi = librarySpecFactory.library(baseName.camelCase)
            val incomingLibraries = if (useIncomingLibraries) usesLibraries else FixedValue(emptyList())
            val library = ExportedLibrarySpec(coordinates, libraryApi, usesPlugins, requiresLibrariesFromThisBuild, implementationLibs, incomingLibraries)
            exportedComponents.producesLibs.add(library)
            return library
        }

        override fun <T> buildSrc(body: BuildBuilder.() -> T): T {
            val buildSrcBaseName = if (owner == null) BaseName("buildSrc") else baseName + "buildSrc"
            val build = BuildBuilderImpl(this, "buildSrc build", buildSrcBaseName, "buildSrc", rootDir.resolve("buildSrc"))
            val result = body(build)
            builds.add(build)
            return result
        }

        override fun requires(plugin: PluginRef) {
            val refImpl = plugin as PluginRefImpl
            usesPlugins.add(refImpl.plugin)
        }

        override fun requires(library: LibraryRef) {
            val refImpl = library as ExportedLibrarySpec
            usesLibraries.add(refImpl.useSpec)
        }

        override fun projectNames(names: List<String>) {
            projectNames = FixedNames(names, baseName.camelCase)
        }

        override fun includeComponents(componentCount: Int) {
            TODO("Not yet implemented")
        }

        override fun toSpec(mapper: Mapper<BuildSpec>): BuildSpec {
            usesPlugins.finished()
            usesLibraries.finished()
            internalComponents.finished()
            exportedComponents.finished()
            producesPlugins.finished()
            val components = FixedComponentsSpec(
                producesPlugins.get(),
                exportedComponents.producesLibs.get() + internalComponents.producesLibs.get(),
                exportedComponents.producesApps.get() + internalComponents.producesApps.get(),
                exportedComponents.implementationLibraries.get() + internalComponents.implementationLibraries.get()
            )
            return BuildSpec(
                displayName,
                rootDir,
                includeConfigurationCacheProblems,
                components,
                mapper.map(children),
                includeSelf
            )
        }
    }

    private class ComponentsBuilder(
        appMapper: Mapper<AppProductionSpec>,
        libMapper: Mapper<ExternalLibraryProductionSpec>,
        internalLibMapper: Mapper<InternalLibraryProductionSpec>
    ) {
        val producesApps = MappingLazyValue(appMapper)
        val producesLibs = MappingLazyValue(libMapper)
        var implementationLibraries = MappingLazyValue(internalLibMapper)

        fun finished() {
            implementationLibraries.finished()
            producesLibs.finished()
            producesApps.finished()
        }
    }
}