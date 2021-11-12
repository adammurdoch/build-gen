package net.rubygrapefruit.gen.templates

/**
 * The structure of the production code in the build tree.
 */
enum class ProductionTreeStructure(val displayName: String) {
    MainBuild("Main build only"),
    ChildBuilds("Child builds"),
    NestedChildBuilds("Nested child builds"),
    ChildBuildsUsesMainBuild("Child builds use main build"),
    ChildBuildsWithCycle("Child builds with cycle");

    override fun toString() = displayName
}