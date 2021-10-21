package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.JvmClassName
import net.rubygrapefruit.gen.specs.ToolingApiClientSpec
import java.net.URI

class ToolingApiClientImplementationAssembler(
    private val sourceFileGenerator: SourceFileGenerator
) {
    fun projectContents(): Assembler<ProjectContentsBuilder> = Assembler.of { _ ->
        if (spec.producesApp is ToolingApiClientSpec) {
            require(spec.usesPlugins.isEmpty())
            require(spec.usesLibraries.isEmpty())

            buildScript.plugin("application")
            buildScript.repository(URI("https://repo.gradle.org/gradle/libs-releases"))
            buildScript.implementationDependency("org.gradle", "gradle-tooling-api", "7.2")
            buildScript.implementationDependency("org.slf4j", "slf4j-simple", "1.7.10")

            val mainClassName = JvmClassName("tooling.client.Main")
            buildScript.block("application") {
                lazyProperty("mainClass", mainClassName.name)
            }

            sourceFileGenerator.java(spec.projectDir.resolve("src/main/java"), mainClassName).apply {
                imports("org.gradle.tooling.GradleConnector")
                method("public static void main(String... args)") {
                    log("Calling tooling API on `${spec.producesApp.targetRootDir}`")
                    variableDefinition("GradleConnector", "connector", "GradleConnector.newConnector()")
                    log("Done")
                }
            }.complete()
        }
    }
}