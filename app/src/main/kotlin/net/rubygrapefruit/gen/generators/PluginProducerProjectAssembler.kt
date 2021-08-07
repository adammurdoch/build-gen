package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.specs.*

class PluginProducerProjectAssembler(
    private val generator: Generator<PluginImplementationSpec>
) {
    fun projectContents(): Assembler<ProjectContentsBuilder> = Assembler.of { generationContext ->
        if (spec.producesPlugins.isNotEmpty()) {
            val plugins = spec.producesPlugins.map {
                when (it) {
                    is CustomPluginProductionSpec -> CustomPluginImplementationSpec(spec, it)
                    is JavaConventionPluginProductionSpec -> JavaConventionPluginImplementationSpec(spec, it)
                }
            }

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

            generationContext.generateInParallel(plugins, generator)
        }
    }
}