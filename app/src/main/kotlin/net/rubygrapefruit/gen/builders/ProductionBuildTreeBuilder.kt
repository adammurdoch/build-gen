package net.rubygrapefruit.gen.builders

private val dataBuildNames = listOf("access", "store")
private val uiBuildNames = listOf("entry", "render")

sealed class ProductionBuildTreeBuilder(val builder: BuildTreeBuilder) {
    val main = builder.mainBuild

    fun <T> main(builder: BuildBuilder.() -> T): T {
        return builder(main)
    }

    fun includeConfigurationCacheProblems() {
        builder.includeConfigurationCacheProblems = true
    }

    fun toolingApiClient() {
        builder.build("tooling-api") {
            producesToolingApiClient()
        }
    }
}

sealed class ProductionBuildTreeBuilderWithSource(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
    init {
        main.appNames("app")
        main.libraryNames("util")
        main.producesApp()
    }
}

class EmptyMainBuildOnlyBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
}

class MainBuildOnlyBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilderWithSource(builder) {
    fun buildSrcPlugin() {
        main {
            val plugin = buildSrc {
                producesPlugin()
            }
            requires(plugin)
        }
    }

    fun childBuildPlugin() {
        main {
            val plugin = pluginBuild("plugins") {
                producesPlugin()
            }
            requires(plugin)
        }
    }
}

class EmptyChildBuildsBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
    val child1 = main.build("ui")
    val child2 = main.build("data")
}

class ChildBuildsBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilderWithSource(builder) {
    val child1 = main.build("ui")
    val child2 = main.build("data")

    init {
        child1.libraryNames(uiBuildNames)
        child2.libraryNames(dataBuildNames)
    }

    fun <T> child1(builder: BuildBuilder.() -> T): T {
        return builder(child1)
    }

    fun <T> child2(builder: BuildBuilder.() -> T): T {
        return builder(child2)
    }

    fun buildSrcPlugin() {
        main {
            val mainPlugin = buildSrc {
                producesPlugin()
            }
            requires(mainPlugin)
        }
        child1 {
            val plugin = buildSrc {
                producesPlugin()
            }
            requires(plugin)
        }
        child2 {
            val plugin = buildSrc {
                producesPlugin()
            }
            requires(plugin)
        }
    }

    fun childBuildPlugin(): BuildBuilder {
        val pluginBuild = main.pluginBuild("plugins")
        val plugin = pluginBuild.producesPlugin()
        main {
            requires(plugin)
        }
        child1 {
            requires(plugin)
        }
        child2 {
            requires(plugin)
        }
        return pluginBuild
    }

    fun <T> childBuildPlugin(builder: BuildBuilder.() -> T): T {
        val pluginBuild = childBuildPlugin()
        return builder(pluginBuild)
    }

    fun mainBuildUsesLibrariesFromChildren() {
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

class NestedChildBuildsBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilderWithSource(builder) {
    val child = main.build("ui")
    val nestedChild = child.build("data")

    init {
        child.libraryNames(uiBuildNames)
        nestedChild.libraryNames(dataBuildNames)
    }

    fun <T> child(builder: BuildBuilder.() -> T): T {
        return builder(child)
    }

    fun <T> nestedChild(builder: BuildBuilder.() -> T): T {
        return builder(nestedChild)
    }

    fun childBuildPlugin() {
        val plugin = main.pluginBuild("plugins") {
            producesPlugin()
        }
        main {
            requires(plugin)
        }
        child {
            requires(plugin)
        }
        nestedChild {
            requires(plugin)
        }
    }
}
