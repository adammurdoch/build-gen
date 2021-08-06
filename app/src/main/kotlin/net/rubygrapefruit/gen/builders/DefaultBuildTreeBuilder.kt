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
        return BuildTreeSpecImpl(rootDir, builds)
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path,
        override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private inner class BuildBuilderImpl(
        val owner: BuildBuilderImpl?,
        override val displayName: String,
        val baseName: BaseName,
        val artifactType: String,
        override val rootDir: Path
    ) : BuildSpec, BuildBuilder {
        private val children = mutableListOf<BuildBuilderImpl>()
        private var pluginBuilds = 0

        override val producesPlugins = mutableListOf<PluginProductionSpec>()
        override val usesPlugins = mutableListOf<PluginUseSpec>()
        override val usesLibraries = mutableListOf<ExternalLibraryUseSpec>()
        override var producesLibraries = mutableListOf<ExternalLibraryProductionSpec>()
        override val childBuilds: List<BuildSpec> = children
        override var projectNames: NameProvider = FixedNames(emptyList(), baseName.camelCase)

        override fun toString(): String {
            return displayName
        }

        override val includeConfigurationCacheProblems: Boolean
            get() = this@DefaultBuildTreeBuilder.includeConfigurationCacheProblems

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

        override fun producesPlugin(): PluginUseSpec {
            val plugin = pluginSpecFactory.plugin(baseName, artifactType, "test.${baseName.lowerCaseDotSeparator}")
            producesPlugins.add(plugin)
            return plugin.toUseSpec()
        }

        override fun producesLibrary(): ExternalLibraryUseSpec {
            val coordinates = ExternalLibraryCoordinates("test.${baseName.lowerCaseDotSeparator}", projectNames.next(), "1.0")
            val libraryApi = librarySpecFactory.library(baseName.camelCase)
            val library = ExternalLibraryProductionSpec(coordinates, libraryApi)
            producesLibraries.add(library)
            return library.toUseSpec()
        }

        override fun producesLibraries(): ExternalLibrariesSpec {
            val top = producesLibrary()
            val bottom = producesLibrary()
            return ExternalLibrariesSpec(top, bottom)
        }

        override fun <T> buildSrc(body: BuildBuilder.() -> T): T {
            val buildSrcBaseName = if (owner == null) BaseName("buildSrc") else baseName + "buildSrc"
            val build = BuildBuilderImpl(this, "buildSrc build", buildSrcBaseName, "buildSrc", rootDir.resolve("buildSrc"))
            val result = body(build)
            builds.add(build)
            return result
        }

        override fun requires(plugin: PluginUseSpec) {
            usesPlugins.add(plugin)
        }

        override fun requires(library: ExternalLibraryUseSpec) {
            usesLibraries.add(library)
        }

        override fun projectNames(names: List<String>) {
            projectNames = FixedNames(names, baseName.camelCase)
        }

        override fun projects(body: RootProjectBuilder.() -> Unit): RootProjectSpec {
            val builder = DefaultRootProjectBuilder(this, librarySpecFactory)
            body(builder)
            return builder.build()
        }
    }
}