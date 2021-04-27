package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

interface LibrarySpecFactory {
    fun library(baseName: String, coordinates: LibraryCoordinates): LibraryProductionSpec

    fun maybeLibrary(baseName: String, coordinates: LibraryCoordinates): LibraryProductionSpec? = library(baseName, coordinates)
}

class NothingLibrarySpecFactory : LibrarySpecFactory {
    override fun maybeLibrary(baseName: String, coordinates: LibraryCoordinates) = null

    override fun library(baseName: String, coordinates: LibraryCoordinates) = throw IllegalStateException()
}

class CustomLibrarySpecFactory : LibrarySpecFactory {
    override fun library(baseName: String, coordinates: LibraryCoordinates) = CustomLibraryProductionSpec(coordinates)
}

class JavaLibrarySpecFactory : LibrarySpecFactory {
    override fun library(baseName: String, coordinates: LibraryCoordinates): LibraryProductionSpec {
        return JavaLibraryProductionSpec(coordinates, JvmMethodReference(JvmClassName("$baseName.api.Api"), "something"))
    }
}