package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.TextFileGenerator
import net.rubygrapefruit.gen.specs.BuildTreeSpec

class BuildTreeAdditionalFilesGenerator(
    private val textFileGenerator: TextFileGenerator
) {
    fun treeContents(): Generator<BuildTreeSpec> = Generator.of {
        textFileGenerator.file(rootDir.resolve("gradle.properties")) {
            println("# Generated file")
            println()
            println("#org.gradle.parallel=true")
        }
        textFileGenerator.file(rootDir.resolve(".gitignore")) {
            println("# Generated file")
            println()
            println(".idea")
            println(".gradle")
            println("build")
        }
    }
}