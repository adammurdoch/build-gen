package net.rubygrapefruit.gen.specs

sealed class LibraryProductionSpec {
    abstract fun toApiSpec(): LibraryApiSpec
}

class CustomLibraryProductionSpec : LibraryProductionSpec() {
    override fun toApiSpec() = CustomLibraryApiSpec()
}

class JavaLibraryProductionSpec(
    val method: JvmMethodReference
) : LibraryProductionSpec() {
    override fun toApiSpec() = JavaLibraryApiSpec(method)
}

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

data class ExternalLibraryCoordinates(
    val group: String,
    val name: String,
    val version: String
) : LibraryCoordinates()