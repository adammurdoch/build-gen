package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.CustomPluginImplementationSpec
import java.io.IOException
import java.nio.file.Files

class CustomPluginImplementationAssembler(
    private val sourceFileGenerator: SourceFileGenerator
) {
    fun pluginImplementation(): Assembler<PluginImplementationBuilder> = Assembler.of { _ ->
        val spec = this.spec
        if (spec is CustomPluginImplementationSpec) {
            sourceFileGenerator.java(spec.project.projectDir.resolve("src/main/java"), spec.taskImplementationClass) {
                imports("org.gradle.api.DefaultTask")
                imports("org.gradle.api.tasks.TaskAction")
                imports("org.gradle.api.tasks.Input")
                imports("org.gradle.api.tasks.OutputFile")
                imports("org.gradle.api.tasks.InputFiles")
                imports("org.gradle.api.provider.Property")
                imports("org.gradle.api.file.RegularFileProperty")
                imports("org.gradle.api.file.ConfigurableFileCollection")
                imports(Files::class)
                imports(IOException::class)
                extends("DefaultTask")
                abstractMethod(
                    """
                        @Input
                        public abstract Property<String> getMessage();
                    """.trimIndent()
                )
                abstractMethod(
                    """
                        @InputFiles
                        public abstract ConfigurableFileCollection getInputFiles();
                    """.trimIndent()
                )
                abstractMethod(
                    """
                        @OutputFile
                        public abstract RegularFileProperty getOutputFile();
                    """.trimIndent()
                )
                method(
                    """
                        @TaskAction
                        public void run() throws IOException {
                            Files.writeString(getOutputFile().get().getAsFile().toPath(), getMessage().get() + "\n");
                            getInputFiles().getFiles();
                            ${source.taskMethodContent}
                        }
                    """.trimIndent()
                )
            }

            val incomingConfiguration = spec.spec.identifier("incoming")
            val outgoingConfiguration = spec.spec.identifier("outgoing")

            source.apply {
                imports("org.gradle.api.tasks.TaskProvider")
                imports("org.gradle.api.artifacts.Configuration")
                imports("org.gradle.api.attributes.Usage")
                applyMethodBody {
                    statements(
                        """
                        project.getPlugins().apply("lifecycle-base");
                        Configuration implementation = project.getConfigurations().maybeCreate("implementation");
                        Configuration incoming = project.getConfigurations().create("$incomingConfiguration");
                        incoming.extendsFrom(implementation);
                        incoming.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "${spec.spec.artifactType}"));
                        incoming.setCanBeConsumed(false);
                        Configuration outgoing = project.getConfigurations().create("$outgoingConfiguration");
                        outgoing.extendsFrom(implementation);
                        outgoing.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, "${spec.spec.artifactType}"));
                        TaskProvider<${spec.taskImplementationClass.simpleName}> worker = project.getTasks().register("${spec.spec.workerTaskName}", ${spec.taskImplementationClass.simpleName}.class, t -> {
                            t.getMessage().set("input");
                            t.getInputFiles().from(incoming);
                            t.getOutputFile().set(project.getLayout().getBuildDirectory().file("${spec.spec.workerTaskName}.txt"));
                        });
                        outgoing.getOutgoing().artifact(worker.flatMap(t -> t.getOutputFile()));
                        TaskProvider<?> lifecycle = project.getTasks().register("${spec.spec.lifecycleTaskName}", t -> {
                            t.dependsOn(worker);
                        });
                        project.getTasks().named("assemble").configure(t -> {
                            t.dependsOn(lifecycle);
                        });
                """.trimIndent()
                    )
                }
            }
        }
    }
}