package net.rubygrapefruit.gen.specs

/**
 * Some logical output of a build.
 */
sealed class BuildComponentProductionSpec(
    // Plugins used by the implementation
    val usesPlugins: List<PluginUseSpec>,

    // Libraries from other builds that are used by the implementation
    val usesLibraries: List<ExternalLibraryUseSpec>,

    // Libraries from the same builds that are used by the implementation
    val usesLibrariesFromSameBuild: List<ExternalLibraryProductionSpec>,

    // Implementation libraries from the same build that are used by the implementation
    val usesImplementationLibraries: List<InternalLibrarySpec>
)