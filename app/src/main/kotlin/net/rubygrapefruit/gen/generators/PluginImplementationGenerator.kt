package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.CustomPluginImplementationSpec
import net.rubygrapefruit.gen.specs.JavaConventionPluginImplementationSpec
import net.rubygrapefruit.gen.specs.PluginImplementationSpec

class PluginImplementationGenerator(
    private val customPluginImplementationGenerator: Generator<CustomPluginImplementationSpec>,
    private val javaConventionPluginImplementationGenerator: Generator<JavaConventionPluginImplementationSpec>
) {
    fun pluginImplementation(): Generator<PluginImplementationSpec> = Generator.of { generationContext ->
        when (this) {
            is CustomPluginImplementationSpec -> customPluginImplementationGenerator.generate(this, generationContext)
            is JavaConventionPluginImplementationSpec -> javaConventionPluginImplementationGenerator.generate(this, generationContext)
        }
    }
}