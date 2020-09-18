package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.specs.PluginImplementationSpec

class PluginProducerGenerator(
        private val generator: Generator<PluginImplementationSpec>
) : Assembler<BuildContentsBuilder> {
    override fun assemble(model: BuildContentsBuilder, generationContext: GenerationContext) {
        val build = model.spec
        if (build.producesPlugins.isEmpty()) {
            return
        }
        model.rootBuildScript.apply {
            plugin("java-gradle-plugin")
            block("gradlePlugin") {
                block("plugins") {
                    build.producesPlugins.forEachIndexed { index, plugin ->
                        namedItem("plugin$index") {
                            property("id", plugin.id)
                            property("implementationClass", plugin.pluginImplementationClass.name)
                        }
                    }
                }
            }
        }

        generationContext.apply(build.producesPlugins.map { PluginImplementationSpec(build, it) }, generator)
    }
}