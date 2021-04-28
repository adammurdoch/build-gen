package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.files.SourceFileGenerator
import net.rubygrapefruit.gen.specs.JavaLibraryApiSpec
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
        for (library in project.usesLibraries) {
            if (library.api is JavaLibraryApiSpec) {
                builder.applyMethodBody("${library.api.methodReference.className.name}.${library.api.methodReference.methodName}(new HashSet<String>());")
            }
        }
        sourceFileGenerator.java(project.projectDir.resolve("src/main/java"), pluginImplementationClass).apply {
            imports("org.gradle.api.Plugin")
            imports("org.gradle.api.Project")
            imports(HashSet::class)
            for (import in builder.imports) {
                imports(import)
            }
            implements("Plugin<Project>")
            method(
                """
                        public void apply(Project project) {
                            System.out.println("apply `${spec.id}`");
                            ${builder.applyMethodContent}
                        }
                    """.trimIndent()
            )
        }.complete()
    }

    private class PluginImplementationBuilderImpl(
        override val spec: PluginImplementationSpec,
        override val includeConfigurationCacheProblems: Boolean
    ) : PluginImplementationBuilder, PluginSourceBuilder {
        val imports = mutableListOf<String>()
        private val applyMethodBody = mutableListOf<String>()
        private val taskMethodBody = mutableListOf<String>()

        val applyMethodContent: String
            get() = applyMethodBody.joinToString("\n")

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

        override fun applyMethodBody(text: String) {
            applyMethodBody.add(text)
        }

        override fun taskMethodBody(text: String) {
            taskMethodBody.add(text)
        }
    }
}