package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.AppImplementationSpec
import net.rubygrapefruit.gen.specs.CustomAppImplementationSpec
import net.rubygrapefruit.gen.specs.JavaAppImplementationSpec

interface ApplicationSpecFactory {
    fun application(): AppImplementationSpec
}

class NothingApplicationSpecFactory : ApplicationSpecFactory {
    override fun application() = throw UnsupportedOperationException()
}

class JavaApplicationSpecFactory : ApplicationSpecFactory {
    override fun application() = JavaAppImplementationSpec()
}

class CustomApplicationSpecFactory : ApplicationSpecFactory {
    override fun application() = CustomAppImplementationSpec()
}