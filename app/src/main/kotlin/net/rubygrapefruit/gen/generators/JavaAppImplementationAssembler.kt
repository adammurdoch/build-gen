package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.JavaAppImplementationSpec

class JavaAppImplementationAssembler(
    private val sourceFileGenerator: SourceFileGenerator
) {
    fun projectContents(): Assembler<ProjectContentsBuilder> = Assembler.of { _ ->
        if (spec.producesApp is JavaAppImplementationSpec) {
            buildScript.plugin("application")
            val mainClassName = spec.producesApp.mainClassName
            buildScript.block("application") {
                property("mainClassName", mainClassName.name)
            }
            sourceFileGenerator.java(spec.projectDir.resolve("src/main/java"), mainClassName).apply {
                method("public static void main(String... args)") {
                    methodCall("System.out.println(\"greetings from " + spec.producesApp.mainClassName.name + "\")")
                }
            }.complete()
        }
    }
}