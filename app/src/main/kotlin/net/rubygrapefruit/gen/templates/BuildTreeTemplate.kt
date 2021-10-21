package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.*

abstract class BuildTreeTemplate(
    val productionBuildTreeStructure: ProductionBuildTreeStructure,
    val buildLogic: BuildLogic
) {
    companion object {
        val MainBuildNoBuildLogic = withEmptyMainBuild {}

        val MainBuildWithBuildSrc = withMainBuild(BuildLogic.BuildSrc) {
            buildSrcPlugin()
        }

        val MainBuildWithPluginChildBuild = withMainBuild(BuildLogic.ChildBuild) {
            childBuildPlugin()
        }

        val MainBuildWithBuildSrcAndPluginChildBuild = withMainBuild(BuildLogic.BuildSrcAndChildBuild) {
            buildSrcPlugin()
            childBuildPlugin()
        }

        val ChildBuildsNoBuildLogic = withEmptyChildBuilds {}

        val ChildBuildsWithBuildSrc = withChildBuilds(BuildLogic.BuildSrc) {
            buildSrcPlugin()
            mainBuildUsesLibrariesFromChildren()
        }

        val ChildBuildsWithPluginChildBuild = withChildBuilds(BuildLogic.ChildBuild) {
            childBuildPlugin()
            mainBuildUsesLibrariesFromChildren()
        }

        val ChildBuildsWithPluginChildBuildAndSharedLibrary = withChildBuilds(BuildLogic.ChildBuildAndSharedLibrary) {
            val sharedLibrary = childBuildPlugin {
                projectNames(listOf("generator", "common"))
                producesLibrary()
            }
            mainBuildUsesLibrariesFromChildren()
            main {
                requires(sharedLibrary)
            }
        }

        val NestedChildBuildsWithPluginChildBuild = withNestedBuilds(BuildLogic.ChildBuild) {
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

        val ChildBuildsWithCycleAndPluginChildBuild = withChildBuilds(ProductionBuildTreeStructure.ChildBuildsWithCycle, BuildLogic.ChildBuild) {
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

        val ChildBuildsUseMainBuildAndWithPluginChildBuild = withChildBuilds(ProductionBuildTreeStructure.ChildBuildsUsesMainBuild, BuildLogic.ChildBuild) {
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

        fun withEmptyMainBuild(body: EmptyMainBuildOnlyBuilder.() -> Unit): BuildTreeTemplate {
            return object : BuildTreeTemplate(ProductionBuildTreeStructure.MainBuild, BuildLogic.None) {
                override fun BuildTreeBuilder.applyTo(): ProductionBuildTreeBuilder {
                    val builder = EmptyMainBuildOnlyBuilder(this)
                    builder.apply {
                        body(builder)
                    }
                    return builder
                }
            }
        }

        fun withEmptyChildBuilds(body: EmptyChildBuildsBuilder.() -> Unit): BuildTreeTemplate {
            return object : BuildTreeTemplate(ProductionBuildTreeStructure.ChildBuilds, BuildLogic.None) {
                override fun BuildTreeBuilder.applyTo(): ProductionBuildTreeBuilder {
                    val builder = EmptyChildBuildsBuilder(this)
                    builder.apply {
                        body(builder)
                    }
                    return builder
                }
            }
        }

        fun withMainBuild(buildLogic: BuildLogic, body: MainBuildOnlyBuilder.() -> Unit): BuildTreeTemplate {
            return object : BuildTreeTemplate(ProductionBuildTreeStructure.MainBuild, buildLogic) {
                override fun BuildTreeBuilder.applyTo(): ProductionBuildTreeBuilder {
                    val builder = MainBuildOnlyBuilder(this)
                    builder.apply {
                        body(builder)
                    }
                    return builder
                }
            }
        }

        fun withChildBuilds(buildLogic: BuildLogic, body: ChildBuildsBuilder.() -> Unit): BuildTreeTemplate {
            return withChildBuilds(ProductionBuildTreeStructure.ChildBuilds, buildLogic, body)
        }

        fun withChildBuilds(structure: ProductionBuildTreeStructure, buildLogic: BuildLogic, body: ChildBuildsBuilder.() -> Unit): BuildTreeTemplate {
            return object : BuildTreeTemplate(structure, buildLogic) {
                override fun BuildTreeBuilder.applyTo(): ProductionBuildTreeBuilder {
                    val builder = ChildBuildsBuilder(this)
                    builder.apply {
                        body(builder)
                    }
                    return builder
                }
            }
        }

        fun withNestedBuilds(buildLogic: BuildLogic, body: NestedChildBuildsBuilder.() -> Unit): BuildTreeTemplate {
            return object : BuildTreeTemplate(ProductionBuildTreeStructure.NestedChildBuilds, buildLogic) {
                override fun BuildTreeBuilder.applyTo(): ProductionBuildTreeBuilder {
                    val builder = NestedChildBuildsBuilder(this)
                    builder.apply {
                        body(builder)
                    }
                    return builder
                }
            }
        }

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
            val options = listOf(TemplateOption.configurationCacheProblems, TemplateOption.largeBuild)
            return ProductionBuildTreeStructure.values().map { production ->
                val trees = BuildLogic.values().map { buildLogic ->
                    val implementations = implementationsFor(buildLogic)
                    val trees = values.filter { it.productionBuildTreeStructure == production && it.buildLogic == buildLogic }.flatMap { template ->
                        implementations.map { implementation ->
                            TreeWithImplementation(template, implementation, options, emptyList())
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

    abstract fun BuildTreeBuilder.applyTo(): ProductionBuildTreeBuilder

    fun applyTo(builder: BuildTreeBuilder, options: List<TemplateOption>) {
        val treeBuilder = run { builder.applyTo() }
        for (option in options) {
            option.run { treeBuilder.applyTo() }
        }
    }
}