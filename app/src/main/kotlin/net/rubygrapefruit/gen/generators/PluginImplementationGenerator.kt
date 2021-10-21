package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.files.JavaSourceFileBuilder
import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.PluginImplementationSpec
import kotlin.reflect.KClass

class PluginImplementationGenerator(
    private val sourceFileGenerator: SourceFileGenerator,
    private val assemblers: List<Assembler<PluginImplementationBuilder>>
) {
    fun pluginImplementation(): Generator<PluginImplementationSpec> = Generator.of { generationContext ->
        val builder = PluginImplementationBuilderImpl(this, project.includeConfigurationCacheProblems)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }
        builder.applyMethodBody { addEntryPoint(project, this) }
        sourceFileGenerator.java(project.projectDir.resolve("src/main/java"), pluginImplementationClass) {
            imports("org.gradle.api.Plugin")
            imports("org.gradle.api.Project")
            imports(Set::class)
            imports(LinkedHashSet::class)
            for (import in builder.imports) {
                imports(import)
            }
            implements("Plugin<Project>")
            method("public void apply(Project project)") {
                log("apply `${spec.id}`")
                for (action in builder.applyMethodBody) {
                    action(this)
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