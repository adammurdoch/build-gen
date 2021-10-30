package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.files.JavaSourceFileBuilder
import net.rubygrapefruit.gen.files.JvmType
import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.PluginImplementationSpec
import kotlin.reflect.KClass

class PluginImplementationGenerator(
    private val sourceFileGenerator: SourceFileGenerator,
    private val assembler: Assembler<PluginImplementationBuilder>
) {
    fun pluginImplementation(): Generator<PluginImplementationSpec> = Generator.of { generationContext ->
        val builder = PluginImplementationBuilderImpl(this, project.includeConfigurationCacheProblems)
        assembler.assemble(builder, generationContext)
        builder.applyMethodBody { addEntryPoint(project) }
        sourceFileGenerator.java(project.projectDir.resolve("src/main/java"), pluginImplementationClass) {
            val projectType = JvmType.type("org.gradle.api.Project")
            val pluginType = JvmType.type("org.gradle.api.Plugin", projectType)
            for (import in builder.imports) {
                imports(import)
            }
            implements(pluginType)
            method("apply", "project", projectType) { project ->
                body {
                    log("apply `${spec.id}`")
                    for (action in builder.applyMethodBody) {
                        action(this)
                    }
                }
            }
        }
    }

    private class PluginImplementationBuilderImpl(
        override val spec: PluginImplementationSpec,
        override val includeConfigurationCacheProblems: Boolean
    ) : PluginImplementationBuilder, PluginSourceBuilder {
        val imports = mutableListOf<String>()
        val applyMethodBody = mutableListOf<JavaSourceFileBuilder.Statements.() -> Unit>()
        private val taskMethodBody = mutableListOf<String>()

        override val taskMethodContent: String
            get() = taskMethodBody.joinToString("\n")

        override val source: PluginSourceBuilder
            get() = this

        override fun imports(name: String) {
            imports.add(name)
        }

        override fun imports(type: KClass<*>) {
            imports.add(type.java.name)
        }

        override fun applyMethodBody(body: JavaSourceFileBuilder.Statements.() -> Unit) {
            applyMethodBody.add(body)
        }

        override fun taskMethodBody(text: String) {
            taskMethodBody.add(text)
        }
    }
}