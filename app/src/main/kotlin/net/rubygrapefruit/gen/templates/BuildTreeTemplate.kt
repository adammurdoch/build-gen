package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder
import net.rubygrapefruit.gen.builders.ChildBuildsBuilder
import net.rubygrapefruit.gen.builders.MainBuildOnlyBuilder
import net.rubygrapefruit.gen.builders.NestedChildBuildsBuilder

enum class BuildTreeTemplate {
    MainBuildNoBuildLogic {
        override fun BuildTreeBuilder.applyTo() {
            MainBuildOnlyBuilder(this)
        }
    },
    MainBuildWithBuildSrc {
        override fun BuildTreeBuilder.applyTo() {
            val builder = MainBuildOnlyBuilder(this)
            builder.apply {
                buildSrcPlugin()
            }
        }
    },
    MainBuildWithPluginChildBuild {
        override fun BuildTreeBuilder.applyTo() {
            val builder = MainBuildOnlyBuilder(this)
            builder.apply {
                childBuildPlugin()
            }
        }
    },
    MainBuildWithBuildSrcAndPluginChildBuild {
        override fun BuildTreeBuilder.applyTo() {
            val builder = MainBuildOnlyBuilder(this)
            builder.apply {
                buildSrcPlugin()
                childBuildPlugin()
            }
        }
    },
    ChildBuildsNoBuildLogic {
        override fun BuildTreeBuilder.applyTo() {
            ChildBuildsBuilder(this)
        }
    },
    ChildBuildsWithBuildSrc {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                buildSrcPlugin()
                val dataLibrary = child1 {
                    producesLibrary()
                }
                val uiLibrary = child2 {
                    producesLibrary()
                }
                main {
                    requires(dataLibrary)
                    requires(uiLibrary)
                }
            }
        }
    },
    ChildBuildsWithPluginChildBuild {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                childBuildPlugin()
                val dataLibrary = child1 {
                    producesLibrary()
                }
                val uiLibrary = child2 {
                    producesLibrary()
                }
                main {
                    requires(dataLibrary)
                    requires(uiLibrary)
                }
            }
        }
    },
    ChildBuildsWithPluginChildBuildAndSharedLibrary {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                val (plugin, sharedLibrary) = main.pluginBuild("shared") {
                    projectNames(listOf("generator", "common"))
                    val plugin = producesPlugin()
                    val library = producesLibrary()
                    Pair(plugin, library)
                }
                val dataLibrary = child1 {
                    requires(plugin)
                    producesLibrary()
                }
                val uiLibrary = child2 {
                    requires(plugin)
                    producesLibrary()
                }
                main {
                    requires(plugin)
                    requires(dataLibrary)
                    requires(uiLibrary)
                    requires(sharedLibrary)
                }
            }
        }
    },
    NestedChildBuildsWithPluginChildBuild {
        override fun BuildTreeBuilder.applyTo() {
            val builder = NestedChildBuildsBuilder(this)
            builder.apply {
                childBuildPlugin()
                val dataLibrary = nestedChild {
                    producesLibrary()
                }
                val uiLibrary = child {
                    requires(dataLibrary)
                    producesLibrary()
                }
                main {
                    requires(uiLibrary)
                }
            }
        }
    },
    CyclicChildBuildsWithPluginChildBuild {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                childBuildPlugin()
                val dataLibrary = child1 {
                    producesLibrary()
                }
                val uiLibrary = child2 {
                    requires(dataLibrary)
                    producesLibrary()
                    producesLibrary()
                }
                main {
                    requires(uiLibrary)
                }
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

    abstract fun BuildTreeBuilder.applyTo()
}