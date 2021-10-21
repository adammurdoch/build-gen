package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.JvmType
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.JvmClassName
import net.rubygrapefruit.gen.specs.ToolingApiClientSpec
import java.io.File
import java.net.URI
import kotlin.io.path.pathString

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
            val fileType = JvmType.type(File::class)

            val srcDir = spec.projectDir.resolve("src/main/java")
            sourceFileGenerator.java(srcDir, mainClassName) {
                imports(File::class)
                staticMethod("main", "args", JvmType.type(String::class).asVarargs) { _ ->
                    body {
                        log("Calling tooling API on `${spec.producesApp.targetRootDir}`")
                        val connector = variableDefinition(connectorType, "connector", connectorType.callStaticMethod("newConnector"))
                        methodCall(connector, "forProjectDirectory", fileType.newInstance(spec.producesApp.targetRootDir.pathString))
                        methodCall(connector, "useGradleVersion", "7.2")
                        val connection = variableDefinition(connectionType, "connection", connector.methodCall("connect"))
                        val action = variableDefinition(executorType, "action", connection.methodCall("action", mainActionType.newInstance()))
                        methodCall("action.setStandardOutput(System.out)")
                        methodCall("action.setStandardError(System.err)")
                        log("[CLIENT] Starting action")
                        methodCall(action, "run")
                        log("[CLIENT] Action finished")
                        methodCall(connection, "close")
                    }
                }
            }

            val nestedActionType = JvmType.type(nestedActionName)
            val listType = JvmType.type(List::class, nestedActionType)
            val arrayListType = JvmType.type(ArrayList::class, nestedActionType)
            val buildType = JvmType.type("org.gradle.tooling.model.gradle.GradleBuild")
            val controllerType = JvmType.type("org.gradle.tooling.BuildController")
            val basicProjectType = JvmType.type("org.gradle.tooling.model.gradle.BasicGradleProject")
            val buildActionOfStringType = JvmType.type("org.gradle.tooling.BuildAction", String::class)

            sourceFileGenerator.java(srcDir, mainActionName) {
                imports(ArrayList::class)
                implements(buildActionOfStringType)
                method("execute", "controller", controllerType) { controller ->
                    returnType(JvmType.type(String::class))
                    body {
                        log("[ACTION] Collecting projects")
                        val root = variableDefinition(buildType, "root", controller.readProperty("buildModel"))
                        val actions = variableDefinition(listType, "actions", arrayListType.newInstance())
                        thisMethodCall("collect", root.reference, actions.reference)
                        iterate(buildType, "build", root.readProperty("editableBuilds")) { build ->
                            thisMethodCall("collect", build.reference, actions.reference)
                        }
                        log("[ACTION] Running actions")
                        methodCall(controller, "run", actions.reference)
                        log("[ACTION] Completing")
                        returnValue("result")
                    }
                }
                method("collect", "build", buildType, "actions", listType) { build, actions ->
                    private()
                    body {
                        methodCall("System.out.println(\"build = \" + build)")
                        iterate(basicProjectType, "project", build.readProperty("projects")) { project ->
                            methodCall("System.out.println(\"project = \" + ${project.name})")
                            methodCall(actions, "add", nestedActionType.newInstance(project.reference))
                        }
                    }
                }
            }

            val publicationsType = JvmType.type("org.gradle.tooling.model.gradle.ProjectPublications")
            val buildActionOfPublicationType = JvmType.type("org.gradle.tooling.BuildAction", publicationsType)

            sourceFileGenerator.java(srcDir, nestedActionName) {
                imports(publicationsType.name)
                implements(buildActionOfPublicationType)
                val project = constructorAndProperty("project", basicProjectType)
                method("execute", "controller", controllerType) { controller ->
                    returnType(publicationsType)
                    body {
                        returnValue(controller.methodCall("findModel", project.reference, publicationsType.asTypeLiteral))
                    }
                }
            }
        }
    }
}