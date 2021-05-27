package net.rubygrapefruit.gen.specs

/**
 * A library consumed by a build and produced by something else.
 */
class ExternalLibraryUseSpec(
    val coordinates: ExternalLibraryCoordinates,
    val spec: LibraryApiSpec
) {
    fun toUseSpec(): LibraryUseSpec = LibraryUseSpec(coordinates, spec)
}

/**
 * A library produced by a build and consumable by some other build.
 */
class ExternalLibraryProductionSpec(
    val coordinates: ExternalLibraryCoordinates,
    val spec: LibraryProductionSpec
) {
    fun toUseSpec(): ExternalLibraryUseSpec = ExternalLibraryUseSpec(coordinates, spec.toApiSpec())
}