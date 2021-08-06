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
                mainBuildUsesLibrariesFromChildren()
            }
        }
    },
    ChildBuildsWithPluginChildBuild {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                childBuildPlugin()
                mainBuildUsesLibrariesFromChildren()
            }
        }
    },
    ChildBuildsWithPluginChildBuildAndSharedLibrary {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                val sharedLibrary = childBuildPlugin {
                    projectNames(listOf("generator", "common"))
                    producesLibrary()
                }
                mainBuildUsesLibrariesFromChildren()
                main {
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
                val child1Library = child1 {
                    producesLibrary()
                }
                val child2Library = child2 {
                    producesLibraries()
                }
                child1 {
                    requires(child2Library.bottom)
                }
                child2 {
                    requires(child1Library)
                }
                main {
                    requires(child2Library.top)
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