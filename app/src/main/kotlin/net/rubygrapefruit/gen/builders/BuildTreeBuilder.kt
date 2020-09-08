package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.PluginSpec
import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.BuildTreeSpec
import java.nio.file.Path

class BuildTreeBuilder(private val rootDir: Path) {
    private val builds = mutableListOf<BuildBuilder>()
    private val mainBuild = BuildBuilder("main build", rootDir)

    init {
        builds.add(mainBuild)
    }

    fun addBuildSrc() {
        val build = BuildBuilder("buildSrc build", rootDir.resolve("buildSrc"))
        val plugin = build.produces("test.buildsrc.plugin")
        mainBuild.requires(plugin)
        builds.add(build)
    }

    fun addBuildLogicBuild() {
        val build = BuildBuilder("build logic build", rootDir.resolve("plugins"))
        val plugin = build.produces("test.plugins.plugin")
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

    private data class BuildBuilder(
            override val displayName: String,
            override val rootDir: Path
    ) : BuildSpec {
        override val producesPlugins = mutableListOf<PluginSpec>()
        override val requiresPlugins = mutableListOf<PluginSpec>()
        override val childBuilds = mutableListOf<BuildSpec>()

        override fun toString(): String {
            return displayName
        }

        fun produces(id: String): PluginSpec {
            val plugin = PluginSpec(id, this)
            producesPlugins.add(plugin)
            return plugin
        }

        fun requires(plugin: PluginSpec) {
            requiresPlugins.add(plugin)
        }
    }
}