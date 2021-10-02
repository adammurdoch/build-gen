package net.rubygrapefruit.gen.specs

class AppProductionSpec(
    val baseName: BaseName,
    usesPlugins: List<PluginUseSpec>,
    usesLibraries: List<ExternalLibraryUseSpec>,
    usesImplementationLibraries: List<InternalLibrarySpec>
) : BuildComponentProductionSpec(usesPlugins, usesLibraries, emptyList(), usesImplementationLibraries)
