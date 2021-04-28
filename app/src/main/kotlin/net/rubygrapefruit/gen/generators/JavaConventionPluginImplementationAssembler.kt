package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.specs.JavaConventionPluginImplementationSpec

class JavaConventionPluginImplementationAssembler {
    fun pluginImplementation(): Assembler<PluginImplementationBuilder> = Assembler.of { generationContext ->
        if (spec is JavaConventionPluginImplementationSpec) {
            source.applyMethodBody("""project.getPluginManager().apply("java-library");""")
        }
    }
}