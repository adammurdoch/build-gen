package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.JavaConventionPluginImplementationSpec
import net.rubygrapefruit.gen.specs.JavaLibraryApiSpec

class JavaConventionPluginImplementationGenerator(
    private val sourceFileGenerator: SourceFileGenerator
) {
    fun pluginImplementation(): Generator<JavaConventionPluginImplementationSpec> = Generator.of { generationContext ->
        sourceFileGenerator.java(project.projectDir.resolve("src/main/java"), pluginImplementationClass).apply {
            imports("org.gradle.api.Plugin")
            imports("org.gradle.api.Project")
            imports(HashSet::class)
            implements("Plugin<Project>")
            method("public void apply(Project project)") {
                methodCall("""System.out.println("apply `${spec.id}`")""")
                methodCall("""project.getPluginManager().apply("java-library")""")
                for (library in project.usesLibraries) {
                    if (library.api is JavaLibraryApiSpec) {
                        methodCall("${library.api.methodReference.className.name}.${library.api.methodReference.methodName}(new HashSet<String>())")
                    }
                }
            }
        }.complete()
    }
}