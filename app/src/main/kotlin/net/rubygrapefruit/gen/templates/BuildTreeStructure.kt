package net.rubygrapefruit.gen.templates

enum class BuildTreeStructure(val displayName: String) {
    MainBuild("Main build"),
    ChildBuilds("Child builds"),
    NestedChildBuilds("Nested child builds");

    override fun toString() = displayName
}