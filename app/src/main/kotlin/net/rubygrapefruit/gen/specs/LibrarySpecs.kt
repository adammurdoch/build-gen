package net.rubygrapefruit.gen.specs

class LibraryUseSpec(
    val coordinates: LibraryCoordinates,
    val api: LibraryApiSpec
)

sealed class LibraryApiSpec

class CustomLibraryApiSpec : LibraryApiSpec()

class JavaLibraryApiSpec(
    val methodReference: JvmMethodReference
) : LibraryApiSpec()

sealed class LibraryCoordinates

class LocalLibraryCoordinates(
    val producedByProject: String
) : LibraryCoordinates()

class ExternalLibraryCoordinates(
    val group: String,
    val name: String,
    val version: String
) : LibraryCoordinates()