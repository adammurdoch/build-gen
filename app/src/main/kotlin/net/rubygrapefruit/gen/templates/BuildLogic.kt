package net.rubygrapefruit.gen.templates

enum class BuildLogic(val displayName: String) {
    None("None"),
    BuildSrc("plugin in buildSrc"),
    ChildBuild("plugin in child build"),
    BuildSrcAndChildBuild("plugin in buildSrc and child build"),
    ChildBuildAndSharedLibrary("plugin in child build with shared library");

    override fun toString() = displayName
}