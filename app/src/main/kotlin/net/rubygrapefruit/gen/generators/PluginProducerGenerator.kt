package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.PluginProductionSpec

class PluginProducerGenerator(
        private val sourceFileGenerator: SourceFileGenerator,
        private val generators: List<PluginGenerator>
) : BuildGenerator {
    override fun generate(context: BuildGenerationContext) {
        val build = context.spec
        if (build.producesPlugins.isNotEmpty()) {
            context.rootBuildScript.apply {
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
                    imports("org.gradle.api.tasks.TaskProvider")
                    implements("Plugin<Project>")
                    val pluginContext = PluginGenerationContextImpl(build, plugin)
                    for (generator in generators) {
                        generator.generate(pluginContext)
                    }
                    method("""
                        public void apply(Project project) {
                            System.out.println("apply ${plugin.id}");
                            project.getPlugins().apply("lifecycle-base");
                            TaskProvider<?> custom = project.getTasks().register("${plugin.taskName}");
                            project.getTasks().named("assemble").configure(t -> {
                                t.dependsOn(custom);
                            });
                            ${pluginContext.formatted}
                        }
                    """.trimIndent())
                }.complete()
            }
        }
    }

    private class PluginGenerationContextImpl(
            override val build: BuildSpec,
            override val spec: PluginProductionSpec
    ) : PluginGenerationContext, PluginSourceBuilder {
        private val content = mutableListOf<String>()

        val formatted: String
            get() = content.joinToString("\n")

        override val source: PluginSourceBuilder
            get() = this

        override fun applyMethodBody(text: String) {
            content.add(text)
        }
    }
}