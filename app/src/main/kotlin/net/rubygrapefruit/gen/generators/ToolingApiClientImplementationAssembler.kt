package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.JavaSourceFileBuilder
import net.rubygrapefruit.gen.files.JvmType
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.files.expression
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
            val fileType = JvmType.type(File::class)
            val systemType = JvmType.type(System::class)

            val srcDir = spec.projectDir.resolve("src/main/java")
            val mainBuildRootDir = spec.producesApp.targetRootDir.toFile().absolutePath

            sourceFileGenerator.java(srcDir, mainClassName) {
                imports(File::class)
                staticMethod("main", "args", JvmType.stringType.asVarargs) { _ ->
                    body {
                        log("Calling tooling API on `${mainBuildRootDir}`")
                        val connector = variableDefinition(connectorType, "connector", connectorType.staticMethodCall("newConnector"))
                        methodCall(connector, "forProjectDirectory", fileType.newInstance(mainBuildRootDir))
                        methodCall(connector, "useGradleVersion", "7.2")
                        val connection = variableDefinition(connectionType, "connection", connector.methodCall("connect"))
                        val action = variableDefinition(executorType, "action", connection.methodCall("action", mainActionType.newInstance()))
                        methodCall(action, "addArguments", "--parallel".expression, "-Dorg.gradle.unsafe.isolated-projects=true".expression)
                        methodCall(action, "setStandardOutput", systemType.field("out"))
                        methodCall(action, "setStandardError", systemType.field("out"))
                        measure("running action", "action") {
                            methodCall(action, "run")
                        }
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
                    returnType(JvmType.stringType)
                    body {
                        measure("action body", "body") {
                            log("Collecting projects")
                            val root = variableDefinition(buildType, "root", controller.readProperty("buildModel"))
                            val actions = variableDefinition(listType, "actions", arrayListType.newInstance())
                            thisMethodCall("collect", root, actions)
                            iterate(buildType, "build", root.readProperty("editableBuilds")) { build ->
                                thisMethodCall("collect", build, actions)
                            }
                            measure("running actions", "actions") {
                                methodCall(controller, "run", actions)
                            }
                        }
                        returnValue("result")
                    }
                }
                method("collect", "build", buildType, "actions", listType) { build, actions ->
                    private()
                    body {
                        log("build = ", build)
                        iterate(basicProjectType, "project", build.readProperty("projects")) { project ->
                            log("project = ", project)
                            methodCall(actions, "add", nestedActionType.newInstance(project))
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
                        returnValue(controller.methodCall("findModel", project, publicationsType.asTypeLiteral))
                    }
                }
            }
        }
    }

    private fun JavaSourceFileBuilder.Statements.measure(description: String, id: String, builder: JavaSourceFileBuilder.Statements.() -> Unit) {
        val systemType = JvmType.type(System::class)
        log("Start $description")
        val startTime = variableDefinition(JvmType.longType, "${id}StartTime", systemType.staticMethodCall("nanoTime"))
        builder(this)
        val endTime = variableDefinition(JvmType.longType, "${id}EndTime", systemType.staticMethodCall("nanoTime"))
        log("Completed $description in ", ((endTime - startTime) / 1000000.toLong().expression) + "ms".expression)
    }
}