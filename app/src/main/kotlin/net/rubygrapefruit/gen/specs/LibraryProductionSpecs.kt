package net.rubygrapefruit.gen.specs

sealed class LibraryProductionSpec {
    abstract fun toUseSpec(): LibraryUseSpec
}

class CustomLibraryProductionSpec(
    private val coordinates: LibraryCoordinates,
) : LibraryProductionSpec() {
    override fun toUseSpec(): LibraryUseSpec {
        return CustomLibraryUseSpec(coordinates)
    }
}

class JavaLibraryProductionSpec(
    private val coordinates: LibraryCoordinates,
    val method: JvmMethodReference
) : LibraryProductionSpec() {
    override fun toUseSpec(): LibraryUseSpec {
        return JavaLibraryUseSpec(coordinates, method)
    }
}
