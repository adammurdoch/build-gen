package net.rubygrapefruit.gen

class BuildTreeBuilder {
    private val builds = mutableListOf<BuildBuilder>()

    init {
        builds.add(BuildBuilder("main build", "."))
    }

    fun addBuildSrc() {
        builds.add(BuildBuilder("buildSrc", "buildSrc"))
    }

    fun addBuildLogicBuild() {
        builds.add(BuildBuilder("build logic", "plugins"))
    }

    fun build(): BuildTreeSpec {
        return BuildTreeSpec(builds.map { it.build() })
    }

    private data class BuildBuilder(val displayName: String, val rootDir: String) {
        override fun toString(): String {
            return displayName
        }

        fun build(): BuildSpec {
            return BuildSpec(displayName, rootDir)
        }
    }
}