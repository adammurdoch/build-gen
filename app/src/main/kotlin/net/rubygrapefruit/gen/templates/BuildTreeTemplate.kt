package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class BuildTreeTemplate(private val display: String) {
    MainBuildOnly("build with no build logic") {
        override val applicableImplementations: List<Implementation>
            get() = listOf(Implementation.None)

        override fun BuildTreeBuilder.applyTo() {
        }
    },
    BuildSrc("build with plugin in buildSrc") {
        override fun BuildTreeBuilder.applyTo() {
            addBuildSrc()
        }
    },
    BuildLogicChildBuild("build with plugin in child build") {
        override fun BuildTreeBuilder.applyTo() {
            val plugin = addBuildLogicBuild()
            mainBuild {
                requires(plugin)
            }
        }
    },
    BuildLogicChildBuildAndBuildSrc("build with plugin in buildSrc and child build") {
        override fun BuildTreeBuilder.applyTo() {
            addBuildSrc()
            val plugin = addBuildLogicBuild()
            mainBuild {
                requires(plugin)
            }
        }
    },
    TreeWithBuildLogicChildBuild("composite build with plugin in child build") {
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
    };

    override fun toString() = display

    open val applicableImplementations: List<Implementation>
        get() = Implementation.values().toList()

    abstract fun BuildTreeBuilder.applyTo()
}