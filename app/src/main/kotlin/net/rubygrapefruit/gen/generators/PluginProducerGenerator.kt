package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.SourceFileGenerator

class PluginProducerGenerator(private val sourceFileGenerator: SourceFileGenerator) : Generator<BuildGenerationContext> {
    override fun generate(model: BuildGenerationContext) {
        val build = model.spec
        if (build.producesPlugins.isNotEmpty()) {
            model.rootBuildScript.apply {
                plugin("java-gradle-plugin")
                block("gradlePlugin") {
                    block("plugins") {
                        build.producesPlugins.forEachIndexed { index, plugin ->
                            namedItem("plugin$index") {
                                property("id", plugin.id)
                                property("implementationClass", plugin.implementationClass.name)
                            }
                        }
                    }
                }
            }

            for (plugin in build.producesPlugins) {
                sourceFileGenerator.java(build.rootDir.resolve("src/main/java"), plugin.implementationClass).apply {
                    imports("org.gradle.api.Plugin")
                    imports("org.gradle.api.Project")
                    implements("Plugin<Project>")
                    method("public void apply(Project project) { System.out.println(\"apply ${plugin.id}\"); }")
                }.complete()
            }
        }
    }
}