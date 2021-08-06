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
    val spec: LibraryProductionSpec,

    // Libraries from other builds that are used by the library implementation
    val usesLibraries: List<ExternalLibraryUseSpec>,

    // Libraries from the same builds that are used by the library implementation
    val usesLibrariesFromSameBuild: List<ExternalLibraryProductionSpec>
)
