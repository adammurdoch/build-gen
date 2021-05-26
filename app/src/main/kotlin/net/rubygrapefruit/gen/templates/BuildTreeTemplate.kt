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
    };

    companion object {
        fun templateFor(treeStructure: BuildTreeStructure, buildLogic: BuildLogic): BuildTreeTemplate {
            return when {
                treeStructure == BuildTreeStructure.MainBuild && buildLogic == BuildLogic.None -> MainBuildNoBuildLogic
                treeStructure == BuildTreeStructure.MainBuild && buildLogic == BuildLogic.BuildSrc -> MainBuildWithBuildSrc
                treeStructure == BuildTreeStructure.MainBuild && buildLogic == BuildLogic.BuildSrcAndChildBuild -> MainBuildWithBuildSrcAndPluginChildBuild
                treeStructure == BuildTreeStructure.MainBuild && buildLogic == BuildLogic.ChildBuild -> MainBuildWithPluginChildBuild
                treeStructure == BuildTreeStructure.ChildBuilds && buildLogic == BuildLogic.None -> ChildBuildsNoBuildLogic
                treeStructure == BuildTreeStructure.ChildBuilds && buildLogic == BuildLogic.BuildSrc -> ChildBuildsWithBuildSrc
                treeStructure == BuildTreeStructure.ChildBuilds && buildLogic == BuildLogic.ChildBuild -> ChildBuildsWithPluginChildBuild
                treeStructure == BuildTreeStructure.ChildBuilds && buildLogic == BuildLogic.ChildBuildAndSharedLibrary -> ChildBuildsWithPluginChildBuildAndSharedLibrary
                treeStructure == BuildTreeStructure.NestedChildBuilds && buildLogic == BuildLogic.ChildBuild -> NestedChildBuildsWithPluginChildBuild
                else -> throw UnsupportedOperationException()
            }
        }
    }

    protected val mainBuildNames = listOf("util", "app")
    protected val dataBuildNames = listOf("main", "store")
    protected val uiBuildNames = listOf("entry", "render")

    abstract fun BuildTreeBuilder.applyTo()
}