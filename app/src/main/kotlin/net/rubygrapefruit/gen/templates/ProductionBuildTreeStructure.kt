package net.rubygrapefruit.gen.templates

/**
 * The structure of the production code in the build tree.
 */
enum class ProductionBuildTreeStructure(val displayName: String) {
    MainBuild("Main build"),
    ChildBuilds("Child builds"),
    NestedChildBuilds("Nested child builds"),
    CyclicChildBuilds("Cycle in child builds");

    override fun toString() = displayName
}