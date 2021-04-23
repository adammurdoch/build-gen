package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class BuildTreeTemplate(private val display: String) {
    MainBuildOnly("build with no build logic") {
        override fun BuildTreeBuilder.applyTo() {
        }
    },
    BuildSrc("build with buildSrc") {
        override fun BuildTreeBuilder.applyTo() {
            addBuildSrc()
        }
    },
    BuildLogicChildBuild("build logic in child build") {
        override fun BuildTreeBuilder.applyTo() {
            val plugin = addBuildLogicBuild()
            mainBuild {
                requires(plugin)
            }
        }
    },
    TreeWithBuildLogicChildBuild("composite build with build logic in child build") {
        override fun BuildTreeBuilder.applyTo() {
            val plugin = addBuildLogicBuild()
            val dataLibrary = addProductionBuild("data") {
                requires(plugin)
            }
            val uiLibrary = addProductionBuild("ui") {
                requires(plugin)
            }
            mainBuild {
                requires(plugin)
                requires(dataLibrary)
                requires(uiLibrary)
            }
        }
    },
    BuildLogicChildBuildAndBuildSrc("build logic in buildSrc and child build") {
        override fun BuildTreeBuilder.applyTo() {
            addBuildSrc()
            val plugin = addBuildLogicBuild()
            mainBuild {
                requires(plugin)
            }
        }
    };

    override fun toString() = display

    abstract fun BuildTreeBuilder.applyTo()
}