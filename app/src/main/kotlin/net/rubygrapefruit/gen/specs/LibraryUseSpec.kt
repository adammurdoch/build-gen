package net.rubygrapefruit.gen.specs

class LibraryUseSpec(
    val coordinates: LibraryCoordinates
)

sealed class LibraryCoordinates

class LocalLibraryCoordinates(
    val producedByProject: String
): LibraryCoordinates()

class ExternalLibraryCoordinates(
    val group: String,
    val name: String,
    val version: String
): LibraryCoordinates()