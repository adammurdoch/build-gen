package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.specs.JavaConventionPluginImplementationSpec

class JavaConventionPluginImplementationAssembler {
    fun pluginImplementation(): Assembler<PluginImplementationBuilder> = Assembler.of { project ->
        if (spec is JavaConventionPluginImplementationSpec) {
            source.applyMethodBody {
                statements("""project.getPluginManager().apply("java-library");""")
            }
        }
    }
}