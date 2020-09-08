package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*
import java.nio.file.Path

class BuildTreeBuilder(private val rootDir: Path) {
    private val builds = mutableListOf<BuildBuilder>()
    private val mainBuild = BuildBuilder("main build", rootDir)

    init {
        builds.add(mainBuild)
    }

    var includeConfigurationCacheProblems = false

    fun addBuildSrc() {
        val build = BuildBuilder("buildSrc build", rootDir.resolve("buildSrc"))
        val plugin = build.produces("test.buildsrc.plugin", "test.buildsrc.PluginImpl")
        mainBuild.requires(plugin)
        builds.add(build)
    }

    fun addBuildLogicBuild() {
        val build = BuildBuilder("build logic build", rootDir.resolve("plugins"))
        val plugin = build.produces("test.plugins.plugin", "test.plugins.PluginImpl")
        mainBuild.requires(plugin)
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
            override val id: String,
            override val implementationClass: JvmClassName,
            override val producedBy: BuildSpec
    ) : PluginProductionSpec, PluginUseSpec

    private inner class BuildBuilder(
            override val displayName: String,
            override val rootDir: Path
    ) : BuildSpec {
        override val producesPlugins = mutableListOf<PluginSpec>()
        override val usesPlugins = mutableListOf<PluginSpec>()
        override val childBuilds = mutableListOf<BuildSpec>()

        override fun toString(): String {
            return displayName
        }

        override val includeConfigurationCacheProblems: Boolean
            get() = this@BuildTreeBuilder.includeConfigurationCacheProblems

        fun produces(id: String, implementationClass: String): PluginSpec {
            val plugin = PluginSpec(id, JvmClassName(implementationClass), this)
            producesPlugins.add(plugin)
            return plugin
        }

        fun requires(plugin: PluginSpec) {
            usesPlugins.add(plugin)
        }
    }
}