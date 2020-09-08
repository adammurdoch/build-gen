package net.rubygrapefruit.gen

class BuildTreeBuilder {
    private val builds = mutableListOf<BuildBuilder>()
    private val mainBuild = BuildBuilder("main build", ".")

    init {
        builds.add(mainBuild)
    }

    fun addBuildSrc() {
        val build = BuildBuilder("buildSrc build", "buildSrc")
        val plugin = build.produces("test.buildsrc.plugin")
        mainBuild.requires(plugin)
        builds.add(build)
    }

    fun addBuildLogicBuild() {
        val build = BuildBuilder("build logic build", "plugins")
        val plugin = build.produces("test.plugins.plugin")
        mainBuild.requires(plugin)
        builds.add(build)
    }

    fun build(): BuildTreeSpec {
        return BuildTreeSpecImpl(builds)
    }

    private class BuildTreeSpecImpl(
            override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private data class BuildBuilder(
            override val displayName: String,
            override val rootDir: String
    ) : BuildSpec {
        override val producesPlugins = mutableListOf<PluginSpec>()
        override val requiresPlugins = mutableListOf<PluginSpec>()

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