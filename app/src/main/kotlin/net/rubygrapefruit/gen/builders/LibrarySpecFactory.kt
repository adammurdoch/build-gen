package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

interface LibrarySpecFactory {
    val canCreate: Boolean
        get() = true

    fun library(baseName: BaseName): LibraryProductionSpec
}

class NothingLibrarySpecFactory : LibrarySpecFactory {
    override val canCreate: Boolean
        get() = false

    override fun library(baseName: BaseName) = throw IllegalStateException()
}

class CustomLibrarySpecFactory : LibrarySpecFactory {
    override fun library(baseName: BaseName) = CustomLibraryProductionSpec()
}

class JavaLibrarySpecFactory : LibrarySpecFactory {
    override fun library(baseName: BaseName): LibraryProductionSpec {
        return JavaLibraryProductionSpec(JvmMethodReference(JvmClassName("${baseName.lowerCaseDotSeparator}.api.${baseName.capitalCase}"), "something"))
    }
}