package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

interface LibrarySpecFactory {
    fun library(baseName: String): LibraryProductionSpec

    fun maybeLibrary(baseName: String): LibraryProductionSpec? = library(baseName)
}

class NothingLibrarySpecFactory : LibrarySpecFactory {
    override fun maybeLibrary(baseName: String) = null

    override fun library(baseName: String) = throw IllegalStateException()
}

class CustomLibrarySpecFactory : LibrarySpecFactory {
    override fun library(baseName: String) = CustomLibraryProductionSpec()
}

class JavaLibrarySpecFactory : LibrarySpecFactory {
    override fun library(baseName: String): LibraryProductionSpec {
        return JavaLibraryProductionSpec(JvmMethodReference(JvmClassName("$baseName.api.Api"), "something"))
    }
}