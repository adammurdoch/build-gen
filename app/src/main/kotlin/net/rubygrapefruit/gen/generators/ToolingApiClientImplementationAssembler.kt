package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.JvmType
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
            val mainActionName = JvmClassName("tooling.client.FetchAllProjectsAction")
            val nestedActionName = JvmClassName("tooling.client.ProjectAction")

            buildScript.block("application") {
                lazyProperty("mainClass", mainClassName.name)
            }

            val connectorType = JvmType.type("org.gradle.tooling.GradleConnector")
            val connectionType = JvmType.type("org.gradle.tooling.ProjectConnection")
            val executorType = JvmType.type("org.gradle.tooling.BuildActionExecuter", String::class)
            val mainActionType = JvmType.type(mainActionName)

            val srcDir = spec.projectDir.resolve("src/main/java")
            sourceFileGenerator.java(srcDir, mainClassName) {
                imports(File::class)
                staticMethod("main", "args", JvmType.type(String::class).asVarargs) { args ->
                    body {
                        log("Calling tooling API on `${spec.producesApp.targetRootDir}`")
                        val connector = variableDefinition(connectorType, "connector", connectorType.callStaticMethod("newConnector"))
                        methodCall("connector.forProjectDirectory(new File(\"${spec.producesApp.targetRootDir}\"))")
                        methodCall("connector.useGradleVersion(\"7.2\")")
                        val connection = variableDefinition(connectionType, "connection", connector.methodCall("connect"))
                        val action = variableDefinition(executorType, "action", connection.methodCall("action", mainActionType.newInstance()))
                        methodCall("action.setStandardOutput(System.out)")
                        methodCall("action.setStandardError(System.err)")
                        log("Starting action:")
                        methodCall("action.run()")
                        log("Action finished")
                        methodCall("connection.close()")
                    }
                }
            }

            val nestedActionType = JvmType.type(nestedActionName)
            val listType = JvmType.type(List::class, nestedActionType)
            val arrayListType = JvmType.type(ArrayList::class, nestedActionType)
            val buildType = JvmType.type("org.gradle.tooling.model.gradle.GradleBuild")
            val controllerType = JvmType.type("org.gradle.tooling.BuildController")

            sourceFileGenerator.java(srcDir, mainActionName) {
                imports(ArrayList::class)
                imports("org.gradle.tooling.BuildAction")
                imports("org.gradle.tooling.BuildController")
                imports("org.gradle.tooling.model.gradle.BasicGradleProject")
                implements("BuildAction<String>")
                method("execute", "controller", controllerType) { controller ->
                    returnType(JvmType.type(String::class))
                    body {
                        log("Running action")
                        variableDefinition(buildType, "root", controller.readProperty("buildModel"))
                        variableDefinition(listType, "actions", arrayListType.newInstance())
                        methodCall("collect(root, actions)")
                        iterate("GradleBuild", "build", "root.getEditableBuilds()") {
                            methodCall("collect(build, actions)")
                        }
                        returnValue("\"result\"")
                    }
                }
                method("collect", "build", buildType, "actions", listType) { build, actions ->
                    private()
                    body {
                        methodCall("System.out.println(\"build = \" + build)")
                        iterate("BasicGradleProject", "project", "build.getProjects()") {
                            methodCall("System.out.println(\"project = \" + project)")
                            methodCall("actions.add(${nestedActionType.newInstance().literal})")
                        }
                    }
                }
            }

            sourceFileGenerator.java(srcDir, nestedActionName) {
                imports("org.gradle.tooling.BuildAction")
                imports("org.gradle.tooling.BuildController")
                imports("org.gradle.tooling.model.gradle.BasicGradleProject")
                implements("BuildAction<String>")
                method("execute", "controller", controllerType) { controller ->
                    returnType(JvmType.type(String::class))
                    body {
                        returnValue("\"result\"")
                    }
                }
            }
        }
    }
}