package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.JavaLibraryProductionSpec

class JavaLibraryImplementationAssembler(
    private val sourceFileGenerator: SourceFileGenerator
) {
    fun projectContents(): Assembler<ProjectContentsBuilder> = Assembler.of { _ ->
        val api = spec.producesLibrary?.spec
        if (api is JavaLibraryProductionSpec) {
            if (spec.usesPlugins.none { it.canProduceJavaLibrary }) {
                buildScript.plugin("java-library")
            }
            sourceFileGenerator.java(spec.projectDir.resolve("src/main/java"), api.method.className) {
                imports(Set::class)
                method("public static void ${api.method.methodName}(Set<String> seen)") {
                    addReferences(spec, this)
                }
            }
        }
    }
}