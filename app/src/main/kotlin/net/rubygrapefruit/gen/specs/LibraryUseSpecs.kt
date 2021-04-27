package net.rubygrapefruit.gen.specs

sealed class LibraryUseSpec(
    val coordinates: LibraryCoordinates
)

class CustomLibraryUseSpec(
    coordinates: LibraryCoordinates,
) : LibraryUseSpec(coordinates)

class JavaLibraryUseSpec(
    coordinates: LibraryCoordinates,
    val methodReference: JvmMethodReference
) : LibraryUseSpec(coordinates)

sealed class LibraryCoordinates

class LocalLibraryCoordinates(
    val producedByProject: String
) : LibraryCoordinates()

class ExternalLibraryCoordinates(
    val group: String,
    val name: String,
    val version: String
) : LibraryCoordinates()