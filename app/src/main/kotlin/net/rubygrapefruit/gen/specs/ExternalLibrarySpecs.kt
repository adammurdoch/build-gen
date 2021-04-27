package net.rubygrapefruit.gen.specs

class ExternalLibraryUseSpec(
    val coordinates: ExternalLibraryCoordinates,
    val spec: LibraryApiSpec
)

class ExternalLibraryProductionSpec(
    val coordinates: ExternalLibraryCoordinates,
    val spec: LibraryProductionSpec
)