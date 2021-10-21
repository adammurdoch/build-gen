package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

interface LibrarySpecFactory {
    val canCreate: Boolean
        get() = true

    fun library(baseName: String): LibraryProductionSpec
}

class NothingLibrarySpecFactory : LibrarySpecFactory {
    override val canCreate: Boolean
        get() = false

    override fun library(baseName: String) = throw IllegalStateException()
}

class CustomLibrarySpecFactory : LibrarySpecFactory {
    override fun library(baseName: String) = CustomLibraryProductionSpec()
}

class JavaLibrarySpecFactory : LibrarySpecFactory {
    override fun library(baseName: String): LibraryProductionSpec {
        return JavaLibraryProductionSpec(JvmMethodReference(JvmClassName("$baseName.api.${baseName.capitalize()}"), "something"))
    }
}