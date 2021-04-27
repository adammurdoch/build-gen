package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class BuildTreeTemplate(private val display: String) {
    MainBuildOnly("Build with no build logic") {
        override val applicableImplementations: List<Implementation>
            get() = listOf(Implementation.None)

        override fun BuildTreeBuilder.applyTo() {
        }
    },
    BuildSrc("Build with plugin in buildSrc") {
        override fun BuildTreeBuilder.applyTo() {
            mainBuild {
                val plugin = buildSrc {
                    producesPlugin()
                }
                requires(plugin)
            }
        }
    },
    BuildLogicChildBuild("Build with plugin in child build") {
        override fun BuildTreeBuilder.applyTo() {
            val plugin = build("plugins") {
                producesPlugin()
            }
            mainBuild {
                requires(plugin)
            }
        }
    },
    BuildLogicChildBuildAndBuildSrc("Build with plugin in buildSrc and child build") {
        override fun BuildTreeBuilder.applyTo() {
            val childBuildPlugin = build("plugins") {
                producesPlugin()
            }
            mainBuild {
                val buildSrcPlugin = buildSrc {
                    producesPlugin()
                }
                requires(buildSrcPlugin)
                requires(childBuildPlugin)
            }
        }
    },
    TreeWithBuildLogicChildBuild("Composite build with plugin in child build") {
        override fun BuildTreeBuilder.applyTo() {
            val plugin = build("plugins") {
                producesPlugin()
            }
            val dataLibrary = build("data") {
                requires(plugin)
                producesLibrary()
            }
            val uiLibrary = build("ui") {
                requires(plugin)
                producesLibrary()
            }
            mainBuild {
                requires(plugin)
                requires(dataLibrary)
                requires(uiLibrary)
            }
        }
    },
    TreeWithBuildLogicAndProductionChildBuild("Composite build with plugin and library in child build") {
        override fun BuildTreeBuilder.applyTo() {
            val (plugin, sharedLibrary) = build("shared") {
                val plugin = producesPlugin()
                val library = producesLibrary()
                Pair(plugin, library)
            }
            val dataLibrary = build("data") {
                requires(plugin)
                producesLibrary()
            }
            val uiLibrary = build("ui") {
                requires(plugin)
                producesLibrary()
            }
            mainBuild {
                requires(plugin)
                requires(dataLibrary)
                requires(uiLibrary)
                requires(sharedLibrary)
            }
        }
    };

    override fun toString() = display

    open val applicableImplementations: List<Implementation>
        get() = Implementation.values().toList()

    abstract fun BuildTreeBuilder.applyTo()
}