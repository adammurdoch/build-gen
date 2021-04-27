package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.CustomPluginImplementationSpec
import net.rubygrapefruit.gen.specs.PluginProductionSpec
import java.io.IOException
import java.nio.file.Files

class CustomPluginImplementationGenerator(
    private val sourceFileGenerator: SourceFileGenerator,
    private val assemblers: List<Assembler<PluginImplementationBuilder>>
) {
    fun pluginImplementation(): Generator<CustomPluginImplementationSpec> = Generator.of { generationContext ->
        val builder = PluginImplementationBuilderImpl(spec, project.includeConfigurationCacheProblems)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }
        sourceFileGenerator.java(project.projectDir.resolve("src/main/java"), taskImplementationClass).apply {
            imports("org.gradle.api.DefaultTask")
            imports("org.gradle.api.tasks.TaskAction")
            imports("org.gradle.api.tasks.Input")
            imports("org.gradle.api.tasks.OutputFile")
            imports("org.gradle.api.tasks.InputFiles")
            imports("org.gradle.api.provider.Property")
            imports("org.gradle.api.file.RegularFileProperty")
            imports("org.gradle.api.file.ConfigurableFileCollection")
            imports(Files::class.java.name)
            imports(IOException::class.java.name)
            extends("DefaultTask")
            abstractMethod("""
                        @Input
                        public abstract Property<String> getMessage();
                    """.trimIndent())
            abstractMethod("""
                        @InputFiles
                        public abstract ConfigurableFileCollection getInputFiles();
                    """.trimIndent())
            abstractMethod("""
                        @OutputFile
                        public abstract RegularFileProperty getOutputFile();
                    """.trimIndent())
            method("""
                        @TaskAction
                        public void run() throws IOException {
                            Files.writeString(getOutputFile().get().getAsFile().toPath(), getMessage().get() + "\n");
                            getInputFiles().getFiles();
                            ${builder.taskMethodContent}
                        }
                    """.trimIndent())
        }.complete()

        val incomingConfiguration = spec.identifier("incoming")
        val outgoingConfiguration = spec.identifier("outgoing")

        sourceFileGenerator.java(project.projectDir.resolve("src/main/java"), pluginImplementationClass).apply {
            imports("org.gradle.api.Plugin")
            imports("org.gradle.api.Project")
            imports("org.gradle.api.tasks.TaskProvider")
            imports("org.gradle.api.artifacts.Configuration")
            imports("org.gradle.api.attributes.Usage")
            implements("Plugin<Project>")
            method("""
                        public void apply(Project project) {
                            System.out.println("apply `${spec.id}`");
                            project.getPlugins().apply("lifecycle-base");
                            Configuration implementation = project.getConfigurations().maybeCreate("implementation");
                            Configuration incoming = project.getConfigurations().create("$incomingConfiguration");
                            incoming.extendsFrom(implementation);
                            incoming.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "${spec.id}"));
                            incoming.setCanBeConsumed(false);
                            Configuration outgoing = project.getConfigurations().create("$outgoingConfiguration");
                            outgoing.extendsFrom(implementation);
                            outgoing.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "${spec.id}"));
                            TaskProvider<${taskImplementationClass.simpleName}> worker = project.getTasks().register("${spec.workerTaskName}", ${taskImplementationClass.simpleName}.class, t -> {
                                t.getMessage().set("input");
                                t.getInputFiles().from(incoming);
                                t.getOutputFile().set(project.getLayout().getBuildDirectory().file("${spec.workerTaskName}.txt"));
                            });
                            outgoing.getOutgoing().artifact(worker.flatMap(t -> t.getOutputFile()));
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
            override val spec: PluginProductionSpec,
            override val includeConfigurationCacheProblems: Boolean
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