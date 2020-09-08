package net.rubygrapefruit.gen.generators

import java.io.PrintWriter
import java.nio.file.Path

class ScriptGenerator(private val dsl: DslLanguage) {
    fun settings(dir: Path, body: SettingsScriptBuilder.() -> Unit) {
        script(dir.resolve("settings.${dsl.extension}")) {
            SettingsScriptBuilderImpl(this).run(body)
        }
    }

    fun build(dir: Path, body: BuildScriptBuilder.() -> Unit) {
        script(dir.resolve("build.${dsl.extension}")) {
            BuildScriptBuilderImpl(this).run(body)
        }
    }

    private fun script(file: Path, body: PrintWriter.() -> Unit) {
        file.toFile().bufferedWriter().use {
            PrintWriter(it).apply {
                println("// GENERATED FILE")
                body(this)
                println()
            }.also { it.flush() }
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

    private open class HasBlockContents : ScriptBlockGenerator {
        private val elements = mutableListOf<BlockElement>()

        override fun block(name: String, body: ScriptBlockGenerator.() -> Unit) {
            val block = NestedBlock(name)
            body(block)
            elements.add(block)
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

    private class NestedBlock(val name: String) : HasBlockContents(), BlockElement {
        override fun renderContents(writer: PrintWriter, prefix: String) {
            writer.print(prefix)
            writer.print(name)
            writer.println(" {")
            renderElements(writer, "$prefix    ")
            writer.print(prefix)
            writer.println("}")
        }
    }

    private class BuildScriptBuilderImpl(val writer: PrintWriter) : HasBlockContents(), BuildScriptBuilder {
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