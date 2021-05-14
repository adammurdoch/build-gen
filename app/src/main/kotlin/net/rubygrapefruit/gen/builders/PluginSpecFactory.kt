package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.BaseName
import net.rubygrapefruit.gen.specs.CustomPluginProductionSpec
import net.rubygrapefruit.gen.specs.JavaConventionPluginProductionSpec
import net.rubygrapefruit.gen.specs.PluginProductionSpec

interface PluginSpecFactory {
    fun plugin(baseName: BaseName, artifactType: String, id: String): PluginProductionSpec
}

class NothingPluginSpecFactory : PluginSpecFactory {
    override fun plugin(baseName: BaseName, artifactType: String, id: String) = throw IllegalStateException()
}

class CustomPluginSpecFactory : PluginSpecFactory {
    override fun plugin(baseName: BaseName, artifactType: String, id: String) = CustomPluginProductionSpec(baseName, artifactType, id)
}

class JavaConventionPluginSpecFactory : PluginSpecFactory {
    override fun plugin(baseName: BaseName, artifactType: String, id: String) = JavaConventionPluginProductionSpec(baseName, id)
}