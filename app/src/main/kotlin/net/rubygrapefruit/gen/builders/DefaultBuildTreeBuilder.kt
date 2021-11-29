package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*
import net.rubygrapefruit.gen.templates.Implementation
import java.nio.file.Path

/**
 * A mutable builder for the build tree structure.
 */
class DefaultBuildTreeBuilder(
    private val rootDir: Path, implementation: Implementation
) : BuildTreeBuilder {
    private val pluginSpecFactory = implementation.pluginSpecFactory
    private val librarySpecFactory = implementation.librarySpecFactory
    private val applicationSpecFactory = implementation.applicationSpecFactory
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

    override fun build(name: String, body: BuildBuilder.() -> Unit) {
        val build = BuildBuilderImpl(null, "build $name", buildBaseName(name), name, rootDir.resolve(name))
        builds.add(build)
        body(build)
    }

    fun build(): BuildTreeSpec {
        val mapper = Mapper<BuildSpec>()
        for (build in builds) {
            build.finalizeIncoming()
        }
        return BuildTreeSpecImpl(rootDir, mapper.map(builds))
    }

    private fun buildBaseName(name: String): BaseName {
        require(!name.contains(':') && !name.contains('/'))
        return BaseName(name)
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path, override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private class PluginRefImpl(
        val owner: BuildBuilderImpl,
        val plugin: PluginsBuilder
    ) : PluginRef

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

    private class DefaultLibraryRef(
        val owner: BuildBuilderImpl,
        val libraries: SingleExternalLibraryBuilder
    ) : LibraryRef

    private class LibrariesRefImpl(
        override val top: DefaultLibraryRef, override val bottom: DefaultLibraryRef
    ) : LibrariesRef

    private inner class BuildBuilderImpl(
        val owner: BuildBuilderImpl?, val displayName: String, val baseName: BaseName, val artifactType: String, val rootDir: Path
    ) : BuildBuilder, Mappable<BuildSpec> {
        private val children = mutableListOf<BuildBuilderImpl>()
        private var pluginBuilds = 0
        private val usesPlugins = CompositePluginsSpec()
        private val usesLibraries = CompositeIncomingLibrariesSpec()
        private val producesPlugins = mutableListOf<PluginsBuilder>()
        private val producesApps = mutableListOf<ApplicationsBuilder>()
        private val defaultNames = FixedNames(baseName.camelCase)
        private val appNames = MutableNames(defaultNames)
        private val pluginNames = MutableNames(defaultNames)
        private val libraryNames = MutableNames(defaultNames)
        private val topLibs = ExternalLibrariesBuilder(libraryNames, "test.${baseName.lowerCaseDotSeparator}", librarySpecFactory)
        private val bottomLibs = ExternalLibrariesBuilder(libraryNames, "test.${baseName.lowerCaseDotSeparator}", librarySpecFactory)
        private val emptyComponents = EmptyComponentsBuilder(libraryNames)
        private val internalComponents = InternalLibrariesBuilder(libraryNames, librarySpecFactory)
        private var includeSelf = false
        private var targetComponentCount: Int? = null

        private val currentComponentCount
            get() = producesPlugins.currentSize + producesApps.currentSize + topLibs.currentSize + bottomLibs.currentSize + internalComponents.currentSize + emptyComponents.currentSize

        init {
            internalComponents.usesPlugins(usesPlugins)
        }

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
            val build = BuildBuilderImpl(this, "build $name", buildBaseName(name), name, rootDir.resolve(name))
            require(!children.any { it.baseName.camelCase == BaseName(name).camelCase })
            children.add(index, build)
            builds.add(build)
            return build
        }

        override fun producesPlugin(): PluginRef {
            val plugin = PluginsBuilder(pluginNames, artifactType, pluginSpecFactory)
            plugin.add()
            producesPlugins.add(plugin)
            return PluginRefImpl(this, plugin)
        }

        override fun producesApp() {
            val app = ApplicationsBuilder(appNames, applicationSpecFactory)
            app.add()
            app.usesPlugins(usesPlugins)
            app.usesLibraries(topLibs.exportedLibraries)
            app.usesLibraries(bottomLibs.exportedLibraries)
            app.usesLibraries(implementationLibs())
            app.usesLibraries(usesLibraries)
            producesApps.add(app)
        }

        override fun producesToolingApiClient() {
            val app = ApplicationsBuilder(appNames, object : ApplicationSpecFactory {
                override fun application(baseName: BaseName): AppImplementationSpec {
                    return ToolingApiClientSpec(main.rootDir)
                }
            })
            app.add()
            producesApps.add(app)
        }

        private fun implementationLibs(): InternalLibrariesSpec {
            if (internalComponents.currentSize == 0) {
                internalComponents.add()
            }
            return internalComponents.exportedLibraries
        }

        private fun addInternalComponent() {
            if (librarySpecFactory.canCreate) {
                internalComponents.add()
            } else {
                emptyComponents.add()
            }
        }

        override fun includeSelf() {
            includeSelf = true
        }

        override fun producesLibrary(): LibraryRef {
            val library = addLibrary(topLibs, usesLibraries, implementationLibs(), ExternalLibrariesSpec.empty)
            return DefaultLibraryRef(this, library)
        }

        override fun producesLibraries(): LibrariesRef {
            val bottom = addLibrary(bottomLibs, IncomingLibrariesSpec.empty, implementationLibs(), ExternalLibrariesSpec.empty)
            val top = addLibrary(topLibs, usesLibraries, InternalLibrariesSpec.empty, bottom.exportedLibraries)
            return LibrariesRefImpl(DefaultLibraryRef(this, top), DefaultLibraryRef(this, bottom))
        }

        private fun addLibrary(container: ExternalLibrariesBuilder, incomingLibraries: IncomingLibrariesSpec, implementationLibs: InternalLibrariesSpec, requiresLibrariesFromThisBuild: ExternalLibrariesSpec): SingleExternalLibraryBuilder {
            val library = container.add()
            library.usesPlugins(usesPlugins)
            library.usesLibraries(requiresLibrariesFromThisBuild)
            library.usesLibraries(implementationLibs)
            library.usesLibraries(incomingLibraries)
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
            require(refImpl.owner != this)
            usesPlugins.add(refImpl.plugin.useSpec)
        }

        override fun requires(library: LibraryRef) {
            val refImpl = library as DefaultLibraryRef
            require(refImpl.owner != this)
            usesLibraries.add(refImpl.libraries.useSpec)
        }

        override fun appNames(vararg names: String) {
            appNames.replace(ChainedNames(names.toList(), defaultNames))
        }

        override fun libraryNames(names: List<String>) {
            libraryNames.replace(ChainedNames(names, defaultNames))
        }

        override fun includeComponents(componentCount: Int) {
            this.targetComponentCount = componentCount
        }

        fun finalizeIncoming() {
            usesPlugins.finalize()
            usesLibraries.finalize()
        }

        override fun toSpec(mapper: Mapper<BuildSpec>): BuildSpec {
            val targetComponents = targetComponentCount
            if (targetComponents != null) {
                while (currentComponentCount < targetComponents) {
                    addInternalComponent()
                }
            }

            val components = FixedComponentsSpec(
                producesPlugins.flatMap { it.contents },
                topLibs.contents + bottomLibs.contents,
                producesApps.flatMap { it.contents },
                internalComponents.contents
            )
            return BuildSpec(
                displayName,
                rootDir,
                includeConfigurationCacheProblems,
                baseName.camelCase,
                components,
                mapper.map(children),
                includeSelf,
                emptyComponents.contents
            )
        }
    }

    private val List<BuildComponentsBuilder<*>>.currentSize: Int
        get() = fold(0) { v, i -> v + i.currentSize }
}