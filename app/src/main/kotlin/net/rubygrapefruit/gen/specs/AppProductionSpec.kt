package net.rubygrapefruit.gen.specs

class AppProductionSpec(
    val baseName: BaseName,
    usesLibraries: List<ExternalLibraryUseSpec>,
    usesImplementationLibraries: List<InternalLibrarySpec>
) : BuildComponentProductionSpec(emptyList(), usesLibraries, emptyList(), usesImplementationLibraries)
