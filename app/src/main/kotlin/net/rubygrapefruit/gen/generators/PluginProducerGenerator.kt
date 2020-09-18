package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.PluginProductionSpec
import java.io.IOException
import java.nio.file.Files

class PluginProducerGenerator(
        private val sourceFileGenerator: SourceFileGenerator,
        private val generators: List<Assembler<PluginGenerationContext>>
) : Assembler<BuildContentsBuilder> {
    override fun assemble(model: BuildContentsBuilder, generationContext: GenerationContext) {
        val build = model.spec
        if (build.producesPlugins.isNotEmpty()) {
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

            for (plugin in build.producesPlugins) {
                val pluginContext = PluginGenerationContextImpl(build, plugin)
                for (generator in generators) {
                    generator.assemble(pluginContext, generationContext)
                }
                sourceFileGenerator.java(build.rootDir.resolve("src/main/java"), plugin.taskImplementationClass).apply {
                    imports("org.gradle.api.DefaultTask")
                    imports("org.gradle.api.tasks.TaskAction")
                    imports("org.gradle.api.tasks.Input")
                    imports("org.gradle.api.tasks.OutputFile")
                    imports("org.gradle.api.provider.Property")
                    imports("org.gradle.api.file.RegularFileProperty")
                    imports(Files::class.java.name)
                    imports(IOException::class.java.name)
                    extends("DefaultTask")
                    abstractMethod("""
                        @Input
                        public abstract Property<String> getMessage();
                    """.trimIndent())
                    abstractMethod("""
                        @OutputFile
                        public abstract RegularFileProperty getOutputFile();
                    """.trimIndent())
                    method("""
                        @TaskAction
                        public void run() throws IOException {
                            Files.writeString(getOutputFile().get().getAsFile().toPath(), getMessage().get() + "\n");
                            ${pluginContext.taskMethodContent}
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
                            TaskProvider<?> worker = project.getTasks().register("${plugin.workerTaskName}", ${plugin.taskImplementationClass.simpleName}.class, t -> {
                                t.getMessage().set("input");
                                t.getOutputFile().set(project.getLayout().getBuildDirectory().file("${plugin.workerTaskName}.txt"));
                            });
                            TaskProvider<?> lifecycle = project.getTasks().register("${plugin.lifecycleTaskName}", t -> {
                                t.dependsOn(worker);
                            });
                            project.getTasks().named("assemble").configure(t -> {
                                t.dependsOn(lifecycle);
                            });
                            ${pluginContext.applyMethodContent}
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
        private val applyMethodBody = mutableListOf<String>()
        private val taskMethodBody = mutableListOf<String>()

        val applyMethodContent: String
            get() = applyMethodBody.joinToString("\n")

        val taskMethodContent: String
            get() = taskMethodBody.joinToString("\n")

        override val source: PluginSourceBuilder
            get() = this

        override fun applyMethodBody(text: String) {
            applyMethodBody.add(text)
        }

        override fun taskMethodBody(text: String) {
            taskMethodBody.add(text)
        }
    }
}