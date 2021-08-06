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
        val mapper = SpecMapper()
        return BuildTreeSpecImpl(rootDir, builds.map { mapper.map(it) })
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path,
        override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private class PluginRefImpl(
        val plugin: PluginUseSpec
    ) : PluginRef

    private class LibraryRefImpl(
        val library: ExternalLibraryUseSpec
    ) : LibraryRef

    private class LibrariesRefImpl(
        override val top: LibraryRef,
        override val bottom: LibraryRef
    ) : LibrariesRef

    private class SpecMapper() {
        private val mappedBuilds = mutableMapOf<BuildBuilderImpl, BuildSpec>()
        private val visiting = mutableSetOf<BuildBuilderImpl>()

        fun map(build: BuildBuilderImpl): BuildSpec {
            val result = mappedBuilds.get(build)
            if (result != null) {
                return result
            }
            if (!visiting.add(build)) {
                throw IllegalStateException("Cycle in build tree.")
            }
            try {
                val mapped = build.toSpec(this)
                mappedBuilds.put(build, mapped)
                return mapped
            } finally {
                visiting.remove(build)
            }
        }
    }

    private inner class BuildBuilderImpl(
        val owner: BuildBuilderImpl?,
        val displayName: String,
        val baseName: BaseName,
        val artifactType: String,
        val rootDir: Path
    ) : BuildBuilder {
        private val children = mutableListOf<BuildBuilderImpl>()
        private var pluginBuilds = 0
        private val producesPlugins = mutableListOf<PluginProductionSpec>()
        private val usesPlugins = mutableListOf<PluginUseSpec>()
        private val usesLibraries = mutableListOf<ExternalLibraryUseSpec>()
        private var producesLibraries = mutableListOf<ExternalLibraryProductionSpec>()
        private val topLevelLibraries = mutableListOf<ExternalLibraryProductionSpec>()
        private val producesApps = mutableListOf<AppProductionSpec>()
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
            producesApps.add(AppProductionSpec(BaseName(projectNames.next())))
        }

        override fun producesLibrary(): LibraryRef {
            val library = addLibrary()
            topLevelLibraries.add(library)
            return LibraryRefImpl(library.toUseSpec())
        }

        override fun producesLibraries(): LibrariesRef {
            val bottom = addLibrary().toUseSpec()
            val top = addLibrary(listOf(bottom))
            topLevelLibraries.add(top)
            return LibrariesRefImpl(LibraryRefImpl(top.toUseSpec()), LibraryRefImpl(bottom))
        }

        private fun addLibrary(requires: List<ExternalLibraryUseSpec> = emptyList()): ExternalLibraryProductionSpec {
            val coordinates = ExternalLibraryCoordinates("test.${baseName.lowerCaseDotSeparator}", projectNames.next(), "1.0")
            val libraryApi = librarySpecFactory.library(baseName.camelCase)
            val library = ExternalLibraryProductionSpec(coordinates, libraryApi, requires)
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
            usesLibraries.add(refImpl.library)
        }

        override fun projectNames(names: List<String>) {
            projectNames = FixedNames(names, baseName.camelCase)
        }

        fun toSpec(mapper: SpecMapper): BuildSpec {
            return BuildSpec(
                displayName,
                rootDir,
                includeConfigurationCacheProblems,
                children.map { mapper.map(it) },
                usesPlugins,
                producesPlugins,
                usesLibraries,
                producesLibraries,
                producesApps,
                topLevelLibraries,
                projectNames,
                librarySpecFactory
            )
        }
    }
}