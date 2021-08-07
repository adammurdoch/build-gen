package net.rubygrapefruit.gen.specs

class AppProductionSpec(
    val baseName: BaseName,

    // Libraries from other builds that are used by the application implementation
    val usesLibraries: List<ExternalLibraryUseSpec>,

    // Implementation libraries from the same build
    val usesImplementationLibraries: List<InternalLibrariesSpec>
)
