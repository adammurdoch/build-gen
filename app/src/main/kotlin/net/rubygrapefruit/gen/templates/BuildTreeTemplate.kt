package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder
import net.rubygrapefruit.gen.builders.ChildBuildsBuilder
import net.rubygrapefruit.gen.builders.MainBuildOnlyBuilder
import net.rubygrapefruit.gen.builders.NestedChildBuildsBuilder

sealed class BuildTreeTemplate(
    val productionBuildTreeStructure: ProductionBuildTreeStructure,
    val buildLogic: BuildLogic
) {
    object MainBuildNoBuildLogic : BuildTreeTemplate(ProductionBuildTreeStructure.MainBuild, BuildLogic.None) {
        override fun BuildTreeBuilder.applyTo() {
            MainBuildOnlyBuilder(this)
        }
    }

    object MainBuildWithBuildSrc : BuildTreeTemplate(ProductionBuildTreeStructure.MainBuild, BuildLogic.BuildSrc) {
        override fun BuildTreeBuilder.applyTo() {
            val builder = MainBuildOnlyBuilder(this)
            builder.apply {
                buildSrcPlugin()
            }
        }
    }


    object MainBuildWithPluginChildBuild : BuildTreeTemplate(ProductionBuildTreeStructure.MainBuild, BuildLogic.ChildBuild) {
        override fun BuildTreeBuilder.applyTo() {
            val builder = MainBuildOnlyBuilder(this)
            builder.apply {
                childBuildPlugin()
            }
        }
    }

    object MainBuildWithBuildSrcAndPluginChildBuild : BuildTreeTemplate(ProductionBuildTreeStructure.MainBuild, BuildLogic.BuildSrcAndChildBuild) {
        override fun BuildTreeBuilder.applyTo() {
            val builder = MainBuildOnlyBuilder(this)
            builder.apply {
                buildSrcPlugin()
                childBuildPlugin()
            }
        }
    }

    object ChildBuildsNoBuildLogic : BuildTreeTemplate(ProductionBuildTreeStructure.ChildBuilds, BuildLogic.None) {
        override fun BuildTreeBuilder.applyTo() {
            ChildBuildsBuilder(this)
        }
    }

    object ChildBuildsWithBuildSrc : BuildTreeTemplate(ProductionBuildTreeStructure.ChildBuilds, BuildLogic.BuildSrc) {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                buildSrcPlugin()
                mainBuildUsesLibrariesFromChildren()
            }
        }
    }

    object ChildBuildsWithPluginChildBuild : BuildTreeTemplate(ProductionBuildTreeStructure.ChildBuilds, BuildLogic.ChildBuild) {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                childBuildPlugin()
                mainBuildUsesLibrariesFromChildren()
            }
        }
    }

    object ChildBuildsWithPluginChildBuildAndSharedLibrary : BuildTreeTemplate(ProductionBuildTreeStructure.ChildBuilds, BuildLogic.ChildBuildAndSharedLibrary) {
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
    }

    object NestedChildBuildsWithPluginChildBuild : BuildTreeTemplate(ProductionBuildTreeStructure.NestedChildBuilds, BuildLogic.ChildBuild) {
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
    }

    object ChildBuildsWithCycleAndPluginChildBuild : BuildTreeTemplate(ProductionBuildTreeStructure.ChildBuildsWithCycle, BuildLogic.ChildBuild) {
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
    }

    object ChildBuildsUseMainBuildAndWithPluginChildBuild : BuildTreeTemplate(ProductionBuildTreeStructure.ChildBuildsUsesMainBuild, BuildLogic.ChildBuild) {
        override fun BuildTreeBuilder.applyTo() {
            val builder = ChildBuildsBuilder(this)
            builder.apply {
                childBuildPlugin()
                val mainLibraries = main.producesLibraries()
                val child1Library = child1 {
                    requires(mainLibraries.bottom)
                    producesLibrary()
                }
                val child2Library = child2 {
                    requires(mainLibraries.bottom)
                    producesLibrary()
                }
                main {
                    includeSelf()
                    requires(child1Library)
                    requires(child2Library)
                }
            }
        }
    }

    companion object {
        fun productionStructures(): List<TreeWithProductionStructure> {
            val values = listOf(
                MainBuildNoBuildLogic,
                MainBuildWithBuildSrc,
                MainBuildWithPluginChildBuild,
                MainBuildWithBuildSrcAndPluginChildBuild,
                ChildBuildsNoBuildLogic,
                ChildBuildsWithBuildSrc,
                ChildBuildsWithPluginChildBuild,
                ChildBuildsWithPluginChildBuildAndSharedLibrary,
                NestedChildBuildsWithPluginChildBuild,
                ChildBuildsWithCycleAndPluginChildBuild,
                ChildBuildsUseMainBuildAndWithPluginChildBuild
            )
            return ProductionBuildTreeStructure.values().map { production ->
                val trees = BuildLogic.values().map { buildLogic ->
                    val implementations = implementationsFor(buildLogic)
                    val trees = values.filter { it.productionBuildTreeStructure == production && it.buildLogic == buildLogic }.flatMap { template ->
                        implementations.map { implementation ->
                            TreeWithImplementation(template, implementation, TemplateOption.values().toList(), emptyList())
                        }
                    }
                    if (trees.isEmpty()) {
                        null
                    } else {
                        TreeWithStructure(buildLogic, trees)
                    }
                }.filterNotNull()
                require(trees.isNotEmpty())
                TreeWithProductionStructure(production, trees)
            }
        }

        private fun implementationsFor(buildLogic: BuildLogic): List<Implementation> {
            return when (buildLogic) {
                BuildLogic.None -> listOf(Implementation.None)
                BuildLogic.ChildBuildAndSharedLibrary -> listOf(Implementation.Java)
                else -> listOf(Implementation.Custom, Implementation.Java)
            }
        }
    }

    abstract fun BuildTreeBuilder.applyTo()
}