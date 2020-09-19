package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.specs.PluginImplementationSpec

class PluginProducerGenerator(
        private val generator: Generator<PluginImplementationSpec>
) {
    fun projectContents(): Assembler<ProjectContentsBuilder> = Assembler.of { generationContext ->
        if (spec.producesPlugins.isNotEmpty()) {
            val plugins = spec.producesPlugins.map { PluginImplementationSpec(spec, it, it.className("PluginImpl"), it.className("WorkerTask")) }

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