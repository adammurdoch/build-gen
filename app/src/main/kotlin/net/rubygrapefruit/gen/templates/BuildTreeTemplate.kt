package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.*

abstract class BuildTreeTemplate(
    val productionTreeStructure: ProductionTreeStructure,
    val buildLogic: BuildLogic
) {
    companion object {
        val mainBuildNoBuildLogic = withEmptyMainBuild {}

        val mainBuildWithBuildSrc = withMainBuild(BuildLogic.BuildSrc) {
            buildSrcPlugin()
        }

        val mainBuildWithPluginChildBuild = withMainBuild(BuildLogic.ChildBuild) {
            childBuildPlugin()
        }

        val mainBuildWithBuildSrcAndPluginChildBuild = withMainBuild(BuildLogic.BuildSrcAndChildBuild) {
            buildSrcPlugin()
            childBuildPlugin()
        }

        val childBuildsNoBuildLogic = withEmptyChildBuilds {}

        val childBuildsWithBuildSrc = withChildBuilds(BuildLogic.BuildSrc) {
            buildSrcPlugin()
            mainBuildUsesLibrariesFromChildren()
        }

        val childBuildsWithPluginChildBuild = withChildBuilds(BuildLogic.ChildBuild) {
            childBuildPlugin()
            mainBuildUsesLibrariesFromChildren()
        }

        val childBuildsWithPluginChildBuildAndSharedLibrary = withChildBuilds(BuildLogic.ChildBuildAndSharedLibrary) {
            val sharedLibrary = childBuildPlugin {
                projectNames(listOf("generator", "common"))
                producesLibrary()
            }
            mainBuildUsesLibrariesFromChildren()
            main {
                requires(sharedLibrary)
            }
        }

        val nestedChildBuildsWithPluginChildBuild = withNestedBuilds(BuildLogic.ChildBuild) {
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

        val childBuildsWithCycleAndPluginChildBuild = withChildBuilds(ProductionTreeStructure.ChildBuildsWithCycle, BuildLogic.ChildBuild) {
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

        val childBuildsUseMainBuildAndWithPluginChildBuild = withChildBuilds(ProductionTreeStructure.ChildBuildsUsesMainBuild, BuildLogic.ChildBuild) {
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

        private fun withEmptyMainBuild(body: EmptyMainBuildOnlyBuilder.() -> Unit): BuildTreeTemplate {
            return template(ProductionTreeStructure.MainBuild, BuildLogic.None, body) { EmptyMainBuildOnlyBuilder(it) }
        }

        private fun withEmptyChildBuilds(body: EmptyChildBuildsBuilder.() -> Unit): BuildTreeTemplate {
            return template(ProductionTreeStructure.ChildBuilds, BuildLogic.None, body) { EmptyChildBuildsBuilder(it) }
        }

        private fun withMainBuild(buildLogic: BuildLogic, body: MainBuildOnlyBuilder.() -> Unit): BuildTreeTemplate {
            return template(ProductionTreeStructure.MainBuild, buildLogic, body) { MainBuildOnlyBuilder(it) }
        }

        private fun withChildBuilds(buildLogic: BuildLogic, body: ChildBuildsBuilder.() -> Unit): BuildTreeTemplate {
            return withChildBuilds(ProductionTreeStructure.ChildBuilds, buildLogic, body)
        }

        private fun withChildBuilds(structure: ProductionTreeStructure, buildLogic: BuildLogic, body: ChildBuildsBuilder.() -> Unit): BuildTreeTemplate {
            return template(structure, buildLogic, body) { ChildBuildsBuilder(it) }
        }

        private fun withNestedBuilds(buildLogic: BuildLogic, body: NestedChildBuildsBuilder.() -> Unit): BuildTreeTemplate {
            return template(ProductionTreeStructure.NestedChildBuilds, buildLogic, body) { NestedChildBuildsBuilder(it) }
        }

        private fun <T : ProductionBuildTreeBuilder> template(productionTreeStructure: ProductionTreeStructure, buildLogic: BuildLogic, body: T.() -> Unit, factory: (BuildTreeBuilder) -> T): BuildTreeTemplate {
            return object : BuildTreeTemplate(productionTreeStructure, buildLogic) {
                override fun BuildTreeBuilder.applyTo(): ProductionBuildTreeBuilder {
                    val builder = factory(this)
                    builder.apply {
                        body(builder)
                    }
                    return builder
                }
            }
        }

        fun productionStructures(): List<ParametersWithProductionStructure> {
            val values = listOf(
                mainBuildNoBuildLogic,
                mainBuildWithBuildSrc,
                mainBuildWithPluginChildBuild,
                mainBuildWithBuildSrcAndPluginChildBuild,
                childBuildsNoBuildLogic,
                childBuildsWithBuildSrc,
                childBuildsWithPluginChildBuild,
                childBuildsWithPluginChildBuildAndSharedLibrary,
                nestedChildBuildsWithPluginChildBuild,
                childBuildsWithCycleAndPluginChildBuild,
                childBuildsUseMainBuildAndWithPluginChildBuild
            )
            val options = listOf(TemplateOption.configurationCacheProblems, TemplateOption.largeBuild, TemplateOption.toolingApiClient).map { BooleanParameter(it) }
            return ProductionTreeStructure.values().map { production ->
                val trees = BuildLogic.values().mapNotNull { buildLogic ->
                    val implementations = implementationsFor(buildLogic)
                    val trees = values.filter { it.productionTreeStructure == production && it.buildLogic == buildLogic }.flatMap { template ->
                        implementations.map { implementation ->
                            Parameters(template, implementation, options)
                        }
                    }
                    if (trees.isEmpty()) {
                        null
                    } else {
                        ParametersWithBuildLogic(buildLogic, trees)
                    }
                }
                require(trees.isNotEmpty())
                ParametersWithProductionStructure(production, trees)
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