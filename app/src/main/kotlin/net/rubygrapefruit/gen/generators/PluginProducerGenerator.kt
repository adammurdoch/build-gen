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
                                property("implementationClass", plugin.pluginImplementationClass.name)
                            }
                        }
                    }
                }
            }

            for (plugin in build.producesPlugins) {
                val pluginContext = PluginGenerationContextImpl(build, plugin)
                for (generator in generators) {
                    generator.generate(pluginContext)
                }
                sourceFileGenerator.java(build.rootDir.resolve("src/main/java"), plugin.taskImplementationClass).apply {
                    imports("org.gradle.api.DefaultTask")
                    imports("org.gradle.api.tasks.TaskAction")
                    extends("DefaultTask")
                    method("""
                        @TaskAction
                        public void run() {
                        }
                    """.trimIndent())
                }.complete()
                sourceFileGenerator.java(build.rootDir.resolve("src/main/java"), plugin.pluginImplementationClass).apply {
                    imports("org.gradle.api.Plugin")
                    imports("org.gradle.api.Project")
                    imports("org.gradle.api.tasks.TaskProvider")
                    implements("Plugin<Project>")
                    method("""
                        public void apply(Project project) {
                            System.out.println("apply `${plugin.id}`");
                            project.getPlugins().apply("lifecycle-base");
                            TaskProvider<?> worker = project.getTasks().register("${plugin.workerTaskName}", ${plugin.taskImplementationClass.simpleName}.class);
                            TaskProvider<?> lifecycle = project.getTasks().register("${plugin.lifecycleTaskName}", t -> {
                                t.dependsOn(worker);
                            });
                            project.getTasks().named("assemble").configure(t -> {
                                t.dependsOn(lifecycle);
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