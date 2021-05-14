package net.rubygrapefruit.gen.templates

enum class BuildLogic(val displayName: String) {
    None("None") {
        override val applicableImplementations: List<Implementation>
            get() = listOf(Implementation.None)
    },
    BuildSrc("plugin in buildSrc"),
    ChildBuild("plugin in child build"),
    BuildSrcAndChildBuild("plugin in build and child build"),
    ChildBuildAndSharedLibrary("plugin in child build uses shared library");

    open val applicableImplementations: List<Implementation>
        get() = listOf(Implementation.Custom, Implementation.Java)

}