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
                lazyProperty("mainClass", mainClassName.name)
            }
            sourceFileGenerator.java(spec.projectDir.resolve("src/main/java"), mainClassName).apply {
                imports(Set::class)
                imports(LinkedHashSet::class)
                method("public static void main(String... args)") {
                    log("greetings from `${spec.name}`")
                    addEntryPoint(spec, this)
                }
            }.complete()
        }
    }
}