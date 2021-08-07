package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*
import java.nio.file.Path

/**
 * A mutable builder for the build tree structure.
 */
class DefaultBuildTreeBuilder(
    private val rootDir: Path,
    private val pluginSpecFactory: PluginSpecFactory,
    private val librarySpecFactory: LibrarySpecFactory
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
        val mapper = Mapper<BuildSpec, Any>(this)
        return BuildTreeSpecImpl(rootDir, mapper.map(builds))
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path,
        override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private class PluginRefImpl(
        val plugin: PluginUseSpec
    ) : PluginRef

    private class LibraryRefImpl(
        val coordinates: ExternalLibraryCoordinates,
        val api: LibraryProductionSpec,
        val useIncomingLibraries: Boolean,
        val requiresLibrariesFromThisBuild: List<LibraryRefImpl>,
        val implementationLibraries: List<InternalLibrariesSpec>
    ) : LibraryRef, Mappable<ExternalLibraryProductionSpec, List<ExternalLibraryUseSpec>> {
        val useSpec = ExternalLibraryUseSpec(coordinates, api.toApiSpec())

        override fun toSpec(param: List<ExternalLibraryUseSpec>, mapper: Mapper<ExternalLibraryProductionSpec, List<ExternalLibraryUseSpec>>): ExternalLibraryProductionSpec {
            val incomingLibraries = if (useIncomingLibraries) param else emptyList()
            return ExternalLibraryProductionSpec(coordinates, api, incomingLibraries, mapper.map(requiresLibrariesFromThisBuild), implementationLibraries)
        }
    }

    private class LibrariesRefImpl(
        override val top: LibraryRefImpl,
        override val bottom: LibraryRefImpl
    ) : LibrariesRef

    private class AppImpl(val baseName: BaseName, val implementationLibraries: List<InternalLibrariesSpec>) {
        fun toSpec(usesLibraries: List<ExternalLibraryUseSpec>) = AppProductionSpec(baseName, usesLibraries, implementationLibraries)
    }

    private interface Mappable<T, P> {
        fun toSpec(param: P, mapper: Mapper<T, P>): T
    }

    private class Mapper<T, P>(val param: P) {
        private val mappedItems = mutableMapOf<Mappable<T, P>, T>()
        private val visiting = mutableSetOf<Mappable<T, P>>()

        fun map(items: List<Mappable<T, P>>): List<T> = items.map { map(it) }

        fun map(item: Mappable<T, P>): T {
            val result = mappedItems[item]
            if (result != null) {
                return result
            }
            if (!visiting.add(item)) {
                throw IllegalStateException("Cycle in object graph.")
            }
            try {
                val mapped = item.toSpec(param, this)
                mappedItems[item] = mapped
                return mapped
            } finally {
                visiting.remove(item)
            }
        }
    }

    private inner class BuildBuilderImpl(
        val owner: BuildBuilderImpl?,
        val displayName: String,
        val baseName: BaseName,
        val artifactType: String,
        val rootDir: Path
    ) : BuildBuilder, Mappable<BuildSpec, Any> {
        private val children = mutableListOf<BuildBuilderImpl>()
        private var pluginBuilds = 0
        private var implementationLibraries = mutableListOf<InternalLibrariesSpec>()
        private val producesPlugins = mutableListOf<PluginProductionSpec>()
        private val usesPlugins = mutableListOf<PluginUseSpec>()
        private val usesLibraries = mutableListOf<ExternalLibraryUseSpec>()
        private val producesLibraries = mutableListOf<LibraryRefImpl>()
        private val producesApps = mutableListOf<AppImpl>()
        private var projectNames: NameProvider = FixedNames(emptyList(), baseName.camelCase)

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
            producesApps.add(AppImpl(BaseName(projectNames.next()), implementationLibs()))
        }

        private fun implementationLibs(): List<InternalLibrariesSpec> {
            if (implementationLibraries.isEmpty()) {
                val libraryName = BaseName(projectNames.next())
                val spec = librarySpecFactory.maybeLibrary(libraryName.camelCase)
                if (spec != null) {
                    implementationLibraries.add(InternalLibrariesSpec(libraryName, spec))
                }
            }
            return implementationLibraries
        }

        override fun producesLibrary(): LibraryRef {
            return addLibrary(true)
        }

        override fun producesLibraries(): LibrariesRef {
            val bottom = addLibrary(false)
            val top = addLibrary(true, listOf(bottom))
            return LibrariesRefImpl(top, bottom)
        }

        private fun addLibrary(useIncomingLibraries: Boolean, requiresLibrariesFromThisBuild: List<LibraryRefImpl> = emptyList()): LibraryRefImpl {
            val coordinates = ExternalLibraryCoordinates("test.${baseName.lowerCaseDotSeparator}", projectNames.next(), "1.0")
            val libraryApi = librarySpecFactory.library(baseName.camelCase)
            val library = LibraryRefImpl(coordinates, libraryApi, useIncomingLibraries, requiresLibrariesFromThisBuild, implementationLibs())
            producesLibraries.add(library)
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
            val refImpl = library as LibraryRefImpl
            usesLibraries.add(refImpl.useSpec)
        }

        override fun projectNames(names: List<String>) {
            projectNames = FixedNames(names, baseName.camelCase)
        }

        override fun toSpec(param: Any, mapper: Mapper<BuildSpec, Any>): BuildSpec {
            val libMapper = Mapper<ExternalLibraryProductionSpec, List<ExternalLibraryUseSpec>>(usesLibraries)
            return BuildSpec(
                displayName,
                rootDir,
                includeConfigurationCacheProblems,
                mapper.map(children),
                usesPlugins,
                producesPlugins,
                libMapper.map(producesLibraries),
                producesApps.map { it.toSpec(usesLibraries) },
                implementationLibraries
            )
        }
    }
}