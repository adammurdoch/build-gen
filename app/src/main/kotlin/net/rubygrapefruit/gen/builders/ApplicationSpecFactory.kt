package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

interface ApplicationSpecFactory {
    fun application(baseName: BaseName): AppImplementationSpec
}

class NothingApplicationSpecFactory : ApplicationSpecFactory {
    override fun application(baseName: BaseName) = throw UnsupportedOperationException()
}

class JavaApplicationSpecFactory : ApplicationSpecFactory {
    override fun application(baseName: BaseName) = JavaAppImplementationSpec(JvmClassName("${baseName.lowerCaseDotSeparator}.impl.${baseName.capitalCase}Main"))
}

class CustomApplicationSpecFactory : ApplicationSpecFactory {
    override fun application(baseName: BaseName) = CustomAppImplementationSpec()
}