package net.rubygrapefruit.gen.specs

sealed class LibraryProductionSpec(
    val coordinates: LibraryCoordinates
) {
    abstract fun toApiSpec(): LibraryApiSpec
}

class CustomLibraryProductionSpec(
    coordinates: LibraryCoordinates,
) : LibraryProductionSpec(coordinates) {
    override fun toApiSpec() = CustomLibraryApiSpec()
}

class JavaLibraryProductionSpec(
    coordinates: LibraryCoordinates,
    val method: JvmMethodReference
) : LibraryProductionSpec(coordinates) {
    override fun toApiSpec() = JavaLibraryApiSpec(method)
}
