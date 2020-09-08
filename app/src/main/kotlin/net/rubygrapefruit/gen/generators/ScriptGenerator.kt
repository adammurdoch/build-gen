package net.rubygrapefruit.gen.generators

import java.io.PrintWriter
import java.nio.file.Path

class ScriptGenerator(private val dsl: DslLanguage, private val textFileGenerator: TextFileGenerator) {
    fun settings(dir: Path, body: SettingsScriptBuilder.() -> Unit) {
        textFileGenerator.file(dir.resolve("settings.${dsl.extension}")) {
            SettingsScriptBuilderImpl(this).run(body)
        }
    }

    fun build(dir: Path, body: BuildScriptBuilder.() -> Unit) {
        textFileGenerator.file(dir.resolve("build.${dsl.extension}")) {
            BuildScriptBuilderImpl(this).run(body)
        }
    }

    private class SettingsScriptBuilderImpl(val writer: PrintWriter) : SettingsScriptBuilder {
        private var includes = false

        override fun includeBuild(path: String) {
            if (!includes) {
                writer.println()
                includes = true
            }
            writer.println("includeBuild(\"$path\")")
        }

        fun run(body: SettingsScriptBuilder.() -> Unit) {
            body(this)
        }
    }

    private interface BlockElement {
        fun renderContents(writer: PrintWriter, prefix: String)
    }

    private class PropertyImpl(val name: String, val value: String) : BlockElement {
        override fun renderContents(writer: PrintWriter, prefix: String) {
            writer.print(prefix)
            writer.print(name)
            writer.print(" = \"")
            writer.print(value)
            writer.println("\"")
        }
    }

    private open inner class HasBlockContents : ScriptBlockGenerator {
        private val elements = mutableListOf<BlockElement>()

        override fun block(name: String, body: ScriptBlockGenerator.() -> Unit) {
            val block = NestedBlock(name)
            body(block)
            elements.add(block)
        }

        override fun namedItem(name: String, body: ScriptBlockGenerator.() -> Unit) {
            if (dsl == DslLanguage.KotlinDsl) {
                block("create(\"${name}\")", body)
            } else {
                block(name, body)
            }
        }

        override fun property(name: String, value: String) {
            elements.add(PropertyImpl(name, value))
        }

        fun renderElements(writer: PrintWriter, prefix: String) {
            for (element in elements) {
                element.renderContents(writer, prefix)
            }
        }
    }

    private inner class NestedBlock(val name: String) : HasBlockContents(), BlockElement {
        override fun renderContents(writer: PrintWriter, prefix: String) {
            writer.print(prefix)
            writer.print(name)
            writer.println(" {")
            renderElements(writer, "$prefix    ")
            writer.print(prefix)
            writer.println("}")
        }
    }

    private inner class BuildScriptBuilderImpl(val writer: PrintWriter) : HasBlockContents(), BuildScriptBuilder {
        private val plugins = mutableListOf<String>()

        override fun plugin(id: String) {
            plugins.add(id)
        }

        fun run(body: BuildScriptBuilder.() -> Unit) {
            body(this)
            if (plugins.isNotEmpty()) {
                writer.println()
                writer.println("plugins {")
                for (plugin in plugins) {
                    writer.println("    id(\"$plugin\")")
                }
                writer.println("}")
            }
            renderElements(writer, "")
        }
    }
}