package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*
import java.nio.file.Path

/**
 * A mutable builder for the build tree structure.
 */
class BuildTreeBuilder(private val rootDir: Path) {
    private val builds = mutableListOf<BuildBuilderImpl>()
    private val mainBuild = BuildBuilderImpl("main build", rootDir, ProjectGraphSpec.AppAndLibraries)

    init {
        builds.add(mainBuild)
    }

    var includeConfigurationCacheProblems = false

    fun addBuildSrc() {
        val build = BuildBuilderImpl("buildSrc build", rootDir.resolve("buildSrc"), ProjectGraphSpec.RootProject)
        val plugin = build.producesPlugin("buildSrc", "test.buildsrc")
        mainBuild.requires(plugin)
        builds.add(build)
    }

    /**
     * Adds a child build that produces a plugin, returning the spec for using the plugin.
     */
    fun addBuildLogicBuild(): PluginUseSpec {
        val build = BuildBuilderImpl("build logic build", rootDir.resolve("plugins"), ProjectGraphSpec.RootProject)
        val plugin = build.producesPlugin("plugins", "test.plugins")
        mainBuild.childBuilds.add(build)
        builds.add(build)
        return plugin
    }

    /**
     * Adds a child build that produces a library, returning the spec for using the library.
     */
    fun addProductionBuild(body: BuildBuilder.() -> Unit): ExternalLibraryUseSpec {
        val build = BuildBuilderImpl("library build", rootDir.resolve("libs"), ProjectGraphSpec.Libraries)
        val library = build.producesLibrary("core")
        body(build)
        mainBuild.childBuilds.add(build)
        builds.add(build)
        return library
    }

    fun mainBuild(body: BuildBuilder.() -> Unit) {
        body(mainBuild)
    }

    fun build(): BuildTreeSpec {
        return BuildTreeSpecImpl(rootDir, builds)
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path,
        override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private class LibrarySpec(val baseName: String) : ExternalLibraryProductionSpec, ExternalLibraryUseSpec {
        override val group: String
            get() = "test.${baseName}"

        override val name: String
            get() = baseName

        override val version: String
            get() = "1.0"
    }

    private class PluginSpec(
        val baseName: String,
        override val id: String,
    ) : PluginProductionSpec, PluginUseSpec {
        override val workerTaskName: String
            get() = identifier("worker")

        override val lifecycleTaskName: String
            get() = baseName

        override fun identifier(suffix: String) = baseName + suffix.capitalize()

        override fun className(classNameSuffix: String): JvmClassName {
            return JvmClassName(id.toLowerCase() + ".plugin." + classNameSuffix.capitalize())
        }
    }

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
            get() = this@BuildTreeBuilder.includeConfigurationCacheProblems

        fun producesPlugin(baseName: String, id: String): PluginSpec {
            val plugin = PluginSpec(baseName, id)
            producesPlugins.add(plugin)
            return plugin
        }

        fun producesLibrary(baseName: String): LibrarySpec {
            val library = LibrarySpec(baseName)
            producesLibrary = library
            return library
        }

        override fun requires(plugin: PluginUseSpec) {
            usesPlugins.add(plugin)
        }

        override fun requires(library: ExternalLibraryUseSpec) {
            usesLibraries.add(library)
        }
    }
}