package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.specs.CustomPluginImplementationSpec
import net.rubygrapefruit.gen.specs.CustomPluginProductionSpec

class CustomPluginProducerGenerator(
    private val generator: Generator<CustomPluginImplementationSpec>
) {
    fun projectContents(): Assembler<ProjectContentsBuilder> = Assembler.of { generationContext ->
        if (spec.producesPlugins.isNotEmpty()) {
            val plugins = spec.producesPlugins.filterIsInstance(CustomPluginProductionSpec::class.java).map { CustomPluginImplementationSpec(spec, it) }

            buildScript.apply {
                plugin("java-gradle-plugin")
                block("gradlePlugin") {
                    block("plugins") {
                        plugins.forEachIndexed { index, plugin ->
                            namedItem("plugin$index") {
                                property("id", plugin.spec.id)
                                property("implementationClass", plugin.pluginImplementationClass.name)
                            }
                        }
                    }
                }
            }

            generationContext.apply(plugins, generator)
        }
    }
}