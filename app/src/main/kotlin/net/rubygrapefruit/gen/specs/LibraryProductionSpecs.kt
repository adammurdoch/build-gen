package net.rubygrapefruit.gen.specs

sealed class LibraryProductionSpec(
    val coordinates: LibraryCoordinates
) {
    abstract fun toUseSpec(): LibraryUseSpec
}

class CustomLibraryProductionSpec(
    coordinates: LibraryCoordinates,
) : LibraryProductionSpec(coordinates) {
    override fun toUseSpec() = CustomLibraryUseSpec(coordinates)
}

class JavaLibraryProductionSpec(
    coordinates: LibraryCoordinates,
    val method: JvmMethodReference
) : LibraryProductionSpec(coordinates) {
    override fun toUseSpec() = JavaLibraryUseSpec(coordinates, method)
}
