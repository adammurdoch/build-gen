package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.JvmClassName
import net.rubygrapefruit.gen.specs.ToolingApiClientSpec
import java.io.File
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
                imports(File::class)
                imports("org.gradle.tooling.GradleConnector")
                imports("org.gradle.tooling.ProjectConnection")
                method("public static void main(String... args)") {
                    log("Calling tooling API on `${spec.producesApp.targetRootDir}`")
                    variableDefinition("GradleConnector", "connector", "GradleConnector.newConnector()")
                    methodCall("connector.forProjectDirectory(new File(\"${spec.producesApp.targetRootDir}\"))")
                    methodCall("connector.useGradleVersion(\"7.2\")")
                    variableDefinition("ProjectConnection", "connection", "connector.connect()")
                    methodCall("connection.close()")
                    log("Done")
                }
            }.complete()

            val actionImplName = JvmClassName("tooling.client.Action")
            sourceFileGenerator.java(spec.projectDir.resolve("src/main/java"), actionImplName).apply {
                imports("org.gradle.tooling.BuildAction")
                imports("org.gradle.tooling.BuildController")
                implements("BuildAction<String>")
                method("public String execute(BuildController controller)") {
                    log("Running action")
                    returnValue("\"result\"")
                }
            }.complete()
        }
    }
}