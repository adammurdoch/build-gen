package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class BuildTreeTemplate {
    MainBuildNoBuildLogic {
        override fun BuildTreeBuilder.applyTo() {
        }
    },
    MainBuildWithBuildSrc {
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
    MainBuildWithPluginChildBuild {
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
    MainBuildWithBuildSrcAndPluginChildBuild {
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
    ChildBuildsNoBuildLogic {
        override fun BuildTreeBuilder.applyTo() {
            mainBuild {
                build("data") {
                }
                build("ui") {
                }
            }
        }
    },
    ChildBuildsWithBuildSrc {
        override fun BuildTreeBuilder.applyTo() {
            mainBuild {
                val mainPlugin = buildSrc {
                    producesPlugin()
                }
                val dataLibrary = build("data") {
                    val plugin = buildSrc {
                        producesPlugin()
                    }
                    requires(plugin)
                    projectNames(dataBuildNames)
                    producesLibrary()
                }
                val uiLibrary = build("ui") {
                    val plugin = buildSrc {
                        producesPlugin()
                    }
                    requires(plugin)
                    projectNames(uiBuildNames)
                    producesLibrary()
                }
                requires(mainPlugin)
                requires(dataLibrary)
                requires(uiLibrary)
                projectNames(mainBuildNames)
            }
        }
    },
    ChildBuildsWithPluginChildBuild {
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
    ChildBuildsWithPluginChildBuildAndSharedLibrary {
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
    NestedChildBuildsWithPluginChildBuild {
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
    },
    CyclicChildBuildsWithPluginChildBuild {
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
                    requires(dataLibrary)
                    projectNames(uiBuildNames)
                    producesLibrary()
                    producesLibrary()
                }
                requires(plugin)
                requires(uiLibrary)
                projectNames(mainBuildNames)
            }
        }
    };

    companion object {
        fun templateFor(treeStructure: ProductionBuildTreeStructure, buildLogic: BuildLogic): BuildTreeTemplate {
            return when {
                treeStructure == ProductionBuildTreeStructure.MainBuild && buildLogic == BuildLogic.None -> MainBuildNoBuildLogic
                treeStructure == ProductionBuildTreeStructure.MainBuild && buildLogic == BuildLogic.BuildSrc -> MainBuildWithBuildSrc
                treeStructure == ProductionBuildTreeStructure.MainBuild && buildLogic == BuildLogic.BuildSrcAndChildBuild -> MainBuildWithBuildSrcAndPluginChildBuild
                treeStructure == ProductionBuildTreeStructure.MainBuild && buildLogic == BuildLogic.ChildBuild -> MainBuildWithPluginChildBuild
                treeStructure == ProductionBuildTreeStructure.ChildBuilds && buildLogic == BuildLogic.None -> ChildBuildsNoBuildLogic
                treeStructure == ProductionBuildTreeStructure.ChildBuilds && buildLogic == BuildLogic.BuildSrc -> ChildBuildsWithBuildSrc
                treeStructure == ProductionBuildTreeStructure.ChildBuilds && buildLogic == BuildLogic.ChildBuild -> ChildBuildsWithPluginChildBuild
                treeStructure == ProductionBuildTreeStructure.ChildBuilds && buildLogic == BuildLogic.ChildBuildAndSharedLibrary -> ChildBuildsWithPluginChildBuildAndSharedLibrary
                treeStructure == ProductionBuildTreeStructure.NestedChildBuilds && buildLogic == BuildLogic.ChildBuild -> NestedChildBuildsWithPluginChildBuild
                treeStructure == ProductionBuildTreeStructure.CyclicChildBuilds && buildLogic == BuildLogic.ChildBuild -> CyclicChildBuildsWithPluginChildBuild
                else -> throw UnsupportedOperationException()
            }
        }

        fun buildLogicOptionsFor(treeStructure: ProductionBuildTreeStructure): List<BuildLogic> {
            return when (treeStructure) {
                ProductionBuildTreeStructure.MainBuild -> listOf(BuildLogic.None, BuildLogic.BuildSrc, BuildLogic.ChildBuild, BuildLogic.BuildSrcAndChildBuild)
                ProductionBuildTreeStructure.ChildBuilds -> listOf(BuildLogic.None, BuildLogic.BuildSrc, BuildLogic.ChildBuild, BuildLogic.ChildBuildAndSharedLibrary)
                ProductionBuildTreeStructure.NestedChildBuilds -> listOf(BuildLogic.ChildBuild)
                ProductionBuildTreeStructure.CyclicChildBuilds -> listOf(BuildLogic.ChildBuild)
            }
        }

        fun implementationsFor(treeStructure: ProductionBuildTreeStructure, buildLogic: BuildLogic): List<Implementation> {
            return when (buildLogic) {
                BuildLogic.None -> listOf(Implementation.None)
                else -> listOf(Implementation.Custom, Implementation.Java)
            }
        }
    }

    protected val mainBuildNames = listOf("util", "app")
    protected val dataBuildNames = listOf("main", "store")
    protected val uiBuildNames = listOf("entry", "render")

    abstract fun BuildTreeBuilder.applyTo()
}