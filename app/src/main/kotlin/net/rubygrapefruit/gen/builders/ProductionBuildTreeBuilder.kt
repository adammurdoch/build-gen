package net.rubygrapefruit.gen.builders

private val mainBuildNames = listOf("util", "app")
private val dataBuildNames = listOf("main", "store")
private val uiBuildNames = listOf("entry", "render")

sealed class ProductionBuildTreeBuilder(val builder: BuildTreeBuilder) {
    val main = builder.mainBuild

    init {
        main.projectNames(mainBuildNames)
    }

    fun <T> main(builder: BuildBuilder.() -> T): T {
        return builder(main)
    }
}

class MainBuildOnlyBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
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

class ChildBuildsBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
    val child1 = main.build("ui")
    val child2 = main.build("data")

    init {
        child1.projectNames(uiBuildNames)
        child2.projectNames(dataBuildNames)
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
    }

    fun childBuildPlugin() {
        main {
            val plugin = pluginBuild("plugins") {
                producesPlugin()
            }
            requires(plugin)
            child1 {
                requires(plugin)
            }
            child2 {
                requires(plugin)
            }
        }
    }
}

class NestedChildBuildsBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
    val child = main.build("ui")
    val nestedChild = child.build("data")

    init {
        child.projectNames(uiBuildNames)
        nestedChild.projectNames(dataBuildNames)
    }

    fun <T> child(builder: BuildBuilder.() -> T): T {
        return builder(child)
    }

    fun <T> nestedChild(builder: BuildBuilder.() -> T): T {
        return builder(nestedChild)
    }

    fun childBuildPlugin() {
        main {
            val plugin = pluginBuild("plugins") {
                producesPlugin()
            }
            requires(plugin)
            child {
                requires(plugin)
            }
            nestedChild {
                requires(plugin)
            }
        }
    }
}
