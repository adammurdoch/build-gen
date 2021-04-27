package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.CustomPluginProductionSpec
import net.rubygrapefruit.gen.specs.JavaConventionPluginProductionSpec
import net.rubygrapefruit.gen.specs.PluginProductionSpec

interface PluginSpecFactory {
    fun plugin(baseName: String, id: String): PluginProductionSpec
}

class NoPluginSpecFactory : PluginSpecFactory {
    override fun plugin(baseName: String, id: String) = throw IllegalStateException()
}

class CustomPluginSpecFactory : PluginSpecFactory {
    override fun plugin(baseName: String, id: String) = CustomPluginProductionSpec(baseName, id)
}

class JavaConventionPluginSpecFactory : PluginSpecFactory {
    override fun plugin(baseName: String, id: String) = JavaConventionPluginProductionSpec(baseName, id)
}