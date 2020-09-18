package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.specs.PluginImplementationSpec

class PluginProducerGenerator(
        private val generator: Generator<PluginImplementationSpec>
) {
    fun buildContents(): Assembler<BuildContentsBuilder> = Assembler.of { generationContext ->
        if (spec.producesPlugins.isNotEmpty()) {
            rootBuildScript.apply {
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