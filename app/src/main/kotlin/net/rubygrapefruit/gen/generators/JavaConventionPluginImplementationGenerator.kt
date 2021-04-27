package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.JavaConventionPluginImplementationSpec

class JavaConventionPluginImplementationGenerator(
    private val sourceFileGenerator: SourceFileGenerator
) {
    fun pluginImplementation(): Generator<JavaConventionPluginImplementationSpec> = Generator.of { generationContext ->
        sourceFileGenerator.java(project.projectDir.resolve("src/main/java"), pluginImplementationClass).apply {
            imports("org.gradle.api.Plugin")
            imports("org.gradle.api.Project")
            implements("Plugin<Project>")
            method(
                """
                        public void apply(Project project) {
                            System.out.println("apply `${spec.id}`");
                            project.getPluginManager().apply("java-library");
                        }
            """.trimIndent()
            )
        }.complete()
    }
}