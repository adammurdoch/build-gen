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
    private val mainBuild = BuildBuilderImpl("main build", rootDir, ProjectGraphSpec.AppAndLibraries)

    init {
        builds.add(mainBuild)
    }

    override var includeConfigurationCacheProblems = false

    override fun addBuildSrc() {
        val build = BuildBuilderImpl("buildSrc build", rootDir.resolve("buildSrc"), ProjectGraphSpec.RootProject)
        val plugin = build.producesPlugin("buildSrc", "test.buildsrc")
        mainBuild.requires(plugin)
        builds.add(build)
    }

    /**
     * Adds a child build that produces a plugin, returning the spec for using the plugin.
     */
    override fun addBuildLogicBuild(): PluginUseSpec {
        val build = BuildBuilderImpl("build logic build", rootDir.resolve("plugins"), ProjectGraphSpec.RootProject)
        val plugin = build.producesPlugin("plugins", "test.plugins")
        mainBuild.childBuilds.add(build)
        builds.add(build)
        return plugin
    }

    /**
     * Adds a child build that produces a library, returning the spec for using the library.
     */
    override fun addProductionBuild(name: String, body: BuildBuilder.() -> Unit): ExternalLibraryUseSpec {
        val build = BuildBuilderImpl("library $name build", rootDir.resolve(name), ProjectGraphSpec.Libraries)
        val library = build.producesLibrary("core", "${name}.core")
        body(build)
        mainBuild.childBuilds.add(build)
        builds.add(build)
        return library
    }

    override fun mainBuild(body: BuildBuilder.() -> Unit) {
        body(mainBuild)
    }

    fun build(): BuildTreeSpec {
        return BuildTreeSpecImpl(rootDir, builds)
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path,
        override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private inner class BuildBuilderImpl(
        override val displayName: String,
        override val rootDir: Path,
        override val projects: ProjectGraphSpec
    ) : BuildSpec, BuildBuilder {
        override val producesPlugins = mutableListOf<PluginProductionSpec>()
        override val usesPlugins = mutableListOf<PluginUseSpec>()
        override val usesLibraries = mutableListOf<ExternalLibraryUseSpec>()
        override var producesLibrary: ExternalLibraryProductionSpec? = null
        override val childBuilds = mutableListOf<BuildSpec>()

        override fun toString(): String {
            return displayName
        }

        override val includeConfigurationCacheProblems: Boolean
            get() = this@DefaultBuildTreeBuilder.includeConfigurationCacheProblems

        fun producesPlugin(baseName: String, id: String): PluginUseSpec {
            val plugin = pluginSpecFactory.plugin(baseName, id)
            producesPlugins.add(plugin)
            return plugin.toUseSpec()
        }

        fun producesLibrary(baseName: String, group: String): ExternalLibraryUseSpec {
            val coordinates = ExternalLibraryCoordinates(group, baseName, "1.0")
            val library = librarySpecFactory.library(baseName, coordinates)
            producesLibrary = ExternalLibraryProductionSpec(coordinates, library)
            return ExternalLibraryUseSpec(coordinates, library.toUseSpec())
        }

        override fun requires(plugin: PluginUseSpec) {
            usesPlugins.add(plugin)
        }

        override fun requires(library: ExternalLibraryUseSpec) {
            usesLibraries.add(library)
        }

        override fun projects(body: RootProjectBuilder.() -> Unit): RootProjectSpec {
            val builder = DefaultRootProjectBuilder(this, librarySpecFactory)
            body(builder)
            return builder.build()
        }
    }
}