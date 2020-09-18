package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.specs.PluginImplementationSpec

class PluginProducerGenerator(
        private val generator: Generator<PluginImplementationSpec>
) {
    fun projectContents(): Assembler<ProjectContentsBuilder> = Assembler.of { generationContext ->
        if (spec.producesPlugins.isNotEmpty()) {
            buildScript.apply {
                plugin("java-gradle-plugin")
                block("gradlePlugin") {
                    block("plugins") {
                        spec.producesPlugins.forEachIndexed { index, plugin ->
                            namedItem("plugin$index") {
                                property("id", plugin.id)
                                property("implementationClass", plugin.pluginImplementationClass.name)
                            }
                        }
                    }
                }
            }

            generationContext.apply(spec.producesPlugins.map { PluginImplementationSpec(spec, it) }, generator)
        }
    }
}