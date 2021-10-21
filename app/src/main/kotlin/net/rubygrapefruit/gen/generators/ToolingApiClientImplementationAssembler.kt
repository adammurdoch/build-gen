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
                imports("org.gradle.tooling.BuildActionExecuter")
                method("public static void main(String... args)") {
                    log("Calling tooling API on `${spec.producesApp.targetRootDir}`")
                    variableDefinition("GradleConnector", "connector", "GradleConnector.newConnector()")
                    methodCall("connector.forProjectDirectory(new File(\"${spec.producesApp.targetRootDir}\"))")
                    methodCall("connector.useGradleVersion(\"7.2\")")
                    variableDefinition("ProjectConnection", "connection", "connector.connect()")
                    variableDefinition("BuildActionExecuter<String>", "action", "connection.action(new Action())")
                    methodCall("action.setStandardOutput(System.out)")
                    methodCall("action.setStandardError(System.err)")
                    log("Starting action:")
                    methodCall("action.run()")
                    log("Action finished")
                    methodCall("connection.close()")
                }
            }.complete()

            val actionImplName = JvmClassName("tooling.client.Action")
            sourceFileGenerator.java(spec.projectDir.resolve("src/main/java"), actionImplName).apply {
                imports("org.gradle.tooling.BuildAction")
                imports("org.gradle.tooling.BuildController")
                imports("org.gradle.tooling.model.gradle.GradleBuild")
                implements("BuildAction<String>")
                method("public String execute(BuildController controller)") {
                    log("Running action")
                    variableDefinition("GradleBuild", "root", "controller.getBuildModel()")
                    methodCall("show(root)")
                    iterate("GradleBuild", "build", "root.getEditableBuilds()") {
                        methodCall("show(build)")
                    }
                    returnValue("\"result\"")
                }
                method("private void show(GradleBuild build)") {
                    methodCall("System.out.println(\"build = \" + build)")
                }
            }.complete()
        }
    }
}