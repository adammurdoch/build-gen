package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.PluginImplementationSpec
import net.rubygrapefruit.gen.specs.PluginProductionSpec
import java.io.IOException
import java.nio.file.Files

class PluginImplementationGenerator(
        private val sourceFileGenerator: SourceFileGenerator,
        private val assemblers: List<Assembler<PluginImplementationBuilder>>
) {
    fun generator(): Generator<PluginImplementationSpec> = Generator.of { generationContext ->
        val builder = PluginImplementationBuilderImpl(build, spec)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }
        sourceFileGenerator.java(build.rootDir.resolve("src/main/java"), spec.taskImplementationClass).apply {
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
                            ${builder.taskMethodContent}
                        }
                    """.trimIndent())
        }.complete()
        sourceFileGenerator.java(build.rootDir.resolve("src/main/java"), spec.pluginImplementationClass).apply {
            imports("org.gradle.api.Plugin")
            imports("org.gradle.api.Project")
            imports("org.gradle.api.tasks.TaskProvider")
            implements("Plugin<Project>")
            method("""
                        public void apply(Project project) {
                            System.out.println("apply `${spec.id}`");
                            project.getPlugins().apply("lifecycle-base");
                            TaskProvider<?> worker = project.getTasks().register("${spec.workerTaskName}", ${spec.taskImplementationClass.simpleName}.class, t -> {
                                t.getMessage().set("input");
                                t.getOutputFile().set(project.getLayout().getBuildDirectory().file("${spec.workerTaskName}.txt"));
                            });
                            TaskProvider<?> lifecycle = project.getTasks().register("${spec.lifecycleTaskName}", t -> {
                                t.dependsOn(worker);
                            });
                            project.getTasks().named("assemble").configure(t -> {
                                t.dependsOn(lifecycle);
                            });
                            ${builder.applyMethodContent}
                        }
                    """.trimIndent())
        }.complete()
    }

    private class PluginImplementationBuilderImpl(
            override val build: BuildSpec,
            override val spec: PluginProductionSpec
    ) : PluginImplementationBuilder, PluginSourceBuilder {
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