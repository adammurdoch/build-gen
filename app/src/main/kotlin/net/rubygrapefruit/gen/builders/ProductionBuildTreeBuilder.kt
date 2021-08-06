package net.rubygrapefruit.gen.builders

sealed class ProductionBuildTreeBuilder(val builder: BuildTreeBuilder) {
    val main = builder.mainBuild

    fun <T> main(builder: BuildBuilder.() -> T): T {
        return builder(main)
    }
}

class MainBuildOnlyBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
}

class ChildBuildsBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
    val child1 = main.build("ui")
    val child2 = main.build("data")

    fun <T> child1(builder: BuildBuilder.() -> T): T {
        return builder(child1)
    }

    fun <T> child2(builder: BuildBuilder.() -> T): T {
        return builder(child2)
    }
}

class NestedChildBuildsBuilder(builder: BuildTreeBuilder) : ProductionBuildTreeBuilder(builder) {
    val child = main.build("ui")
    val nestedChild = child.build("data")

    fun <T> child(builder: BuildBuilder.() -> T): T {
        return builder(child)
    }

    fun <T> nestedChild(builder: BuildBuilder.() -> T): T {
        return builder(nestedChild)
    }
}
