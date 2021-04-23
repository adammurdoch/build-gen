package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*
import java.nio.file.Path

class BuildTreeBuilder(private val rootDir: Path) {
    private val builds = mutableListOf<BuildBuilder>()
    private val mainBuild = BuildBuilder("main build", rootDir, ProjectGraphSpec.MultipleProjects)

    init {
        builds.add(mainBuild)
    }

    var includeConfigurationCacheProblems = false

    fun addBuildSrc() {
        val build = BuildBuilder("buildSrc build", rootDir.resolve("buildSrc"), ProjectGraphSpec.RootProject)
        val plugin = build.produces("buildSrc", "test.buildsrc")
        mainBuild.requires(plugin)
        builds.add(build)
    }

    fun addBuildLogicBuild(): PluginUseSpec {
        val build = BuildBuilder("build logic build", rootDir.resolve("plugins"), ProjectGraphSpec.RootProject)
        val plugin = build.produces("plugins", "test.plugins")
        mainBuild.childBuilds.add(build)
        builds.add(build)
        return plugin
    }

    fun mainBuild(body: BuildRelationshipBuilder.() -> Unit) {
        body(mainBuild)
    }

    fun addProductionBuild(body: BuildRelationshipBuilder.() -> Unit) {
        val build = BuildBuilder("library build", rootDir.resolve("libs"), ProjectGraphSpec.MultipleProjects)
        body(build)
        mainBuild.childBuilds.add(build)
        builds.add(build)
    }

    fun build(): BuildTreeSpec {
        return BuildTreeSpecImpl(rootDir, builds)
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path,
        override val builds: List<BuildSpec>
    ) : BuildTreeSpec

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

    private inner class BuildBuilder(
        override val displayName: String,
        override val rootDir: Path,
        override val projects: ProjectGraphSpec
    ) : BuildSpec, BuildRelationshipBuilder {
        override val producesPlugins = mutableListOf<PluginSpec>()
        override val usesPlugins = mutableListOf<PluginUseSpec>()
        override val childBuilds = mutableListOf<BuildSpec>()

        override fun toString(): String {
            return displayName
        }

        override val includeConfigurationCacheProblems: Boolean
            get() = this@BuildTreeBuilder.includeConfigurationCacheProblems

        fun produces(baseName: String, id: String): PluginSpec {
            val plugin = PluginSpec(baseName, id)
            producesPlugins.add(plugin)
            return plugin
        }

        override fun requires(plugin: PluginUseSpec) {
            usesPlugins.add(plugin)
        }
    }
}