package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.JvmType
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
                val set = JvmType.type(Set::class, String::class)
                staticMethod(api.method.methodName, "seen", set) { seen ->
                    body {
                        addReferences(spec, seen, this)
                    }
                }
            }
        }
    }
}