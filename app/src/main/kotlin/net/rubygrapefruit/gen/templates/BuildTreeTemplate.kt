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
                projectNames(mainBuildNames)
            }
        }
    },
    BuildLogicChildBuild("Build with plugin in child build") {
        override fun BuildTreeBuilder.applyTo() {
            mainBuild {
                val plugin = build("plugins") {
                    producesPlugin()
                }
                requires(plugin)
                projectNames(mainBuildNames)
            }
        }
    },
    BuildLogicChildBuildAndBuildSrc("Build with plugin in buildSrc and child build") {
        override fun BuildTreeBuilder.applyTo() {
            mainBuild {
                val buildSrcPlugin = buildSrc {
                    producesPlugin()
                }
                val childBuildPlugin = build("plugins") {
                    producesPlugin()
                }
                requires(buildSrcPlugin)
                requires(childBuildPlugin)
                projectNames(mainBuildNames)
            }
        }
    },
    TreeWithBuildLogicChildBuild("Composite build with plugin in child build") {
        override fun BuildTreeBuilder.applyTo() {
            mainBuild {
                val plugin = build("plugins") {
                    producesPlugin()
                }
                val dataLibrary = build("data") {
                    requires(plugin)
                    projectNames(dataBuildNames)
                    producesLibrary()
                }
                val uiLibrary = build("ui") {
                    requires(plugin)
                    projectNames(uiBuildNames)
                    producesLibrary()
                }
                requires(plugin)
                requires(dataLibrary)
                requires(uiLibrary)
                projectNames(mainBuildNames)
            }
        }
    },
    TreeWithBuildLogicAndLibraryChildBuild("Composite build with plugin and library in child build") {
        override val applicableImplementations: List<Implementation>
            get() = listOf(Implementation.Java)

        override fun BuildTreeBuilder.applyTo() {
            mainBuild {
                val (plugin, sharedLibrary) = build("shared") {
                    projectNames(listOf("generator", "common"))
                    val plugin = producesPlugin()
                    val library = producesLibrary()
                    Pair(plugin, library)
                }
                val dataLibrary = build("data") {
                    requires(plugin)
                    projectNames(dataBuildNames)
                    producesLibrary()
                }
                val uiLibrary = build("ui") {
                    requires(plugin)
                    projectNames(uiBuildNames)
                    producesLibrary()
                }
                requires(plugin)
                requires(dataLibrary)
                requires(uiLibrary)
                requires(sharedLibrary)
                projectNames(mainBuildNames)
            }
        }
    },
    TreeWithNestedChildBuild("Composite build with nested child builds") {
        override fun BuildTreeBuilder.applyTo() {
            mainBuild {
                val plugin = build("plugins") {
                    producesPlugin()
                }
                val uiLibrary = build("ui") {
                    requires(plugin)
                    val dataLibrary = build("data") {
                        requires(plugin)
                        projectNames(dataBuildNames)
                        producesLibrary()
                    }
                    requires(dataLibrary)
                    projectNames(uiBuildNames)
                    producesLibrary()
                }
                requires(plugin)
                requires(uiLibrary)
                projectNames(mainBuildNames)
            }
        }
    };

    protected val mainBuildNames = listOf("util", "app")
    protected val dataBuildNames = listOf("main", "store")
    protected val uiBuildNames = listOf("entry", "render")

    override fun toString() = display

    open val applicableImplementations: List<Implementation>
        get() = Implementation.values().toList()

    abstract fun BuildTreeBuilder.applyTo()
}