package net.rubygrapefruit.gen.templates

/**
 * The structure of the production code in the build tree.
 */
enum class ProductionBuildTreeStructure(val displayName: String) {
    MainBuild("Main build"),
    ChildBuilds("Child builds"),
    NestedChildBuilds("Nested child builds"),
    ChildBuildsUsesMainBuild("Child builds use main build"),
    ChildBuildsWithCycle("Child builds with cycle");

    override fun toString() = displayName
}