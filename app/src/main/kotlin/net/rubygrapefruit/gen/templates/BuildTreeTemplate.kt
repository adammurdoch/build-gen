package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class BuildTreeTemplate(private val display: String) {
    MainBuildOnly("build with no build logic") {
        override fun applyTo(builder: BuildTreeBuilder) {
        }
    },
    BuildSrc("build with buildSrc") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.addBuildSrc()
        }
    },
    BuildLogicChildBuild("build logic in child build") {
        override fun applyTo(builder: BuildTreeBuilder) {
            val plugin = builder.addBuildLogicBuild()
            builder.mainBuild {
                requires(plugin)
            }
        }
    },
    TreeWithBuildLogicChildBuild("composite build with build logic in child build") {
        override fun applyTo(builder: BuildTreeBuilder) {
            val plugin = builder.addBuildLogicBuild()
            val library = builder.addProductionBuild {
                requires(plugin)
            }
            builder.mainBuild {
                requires(plugin)
                requires(library)
            }
        }
    },
    BuildLogicChildBuildAndBuildSrc("build logic in buildSrc and child build") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.addBuildSrc()
            builder.addBuildLogicBuild()
        }
    };

    override fun toString() = display

    abstract fun applyTo(builder: BuildTreeBuilder)
}