package net.rubygrapefruit.gen.files

import java.io.PrintWriter
import java.nio.file.Path

class ScriptGenerator(private val dsl: DslLanguage, private val textFileGenerator: TextFileGenerator) {
    fun settings(dir: Path): SettingsScriptBuilder {
        return SettingsScriptBuilderImpl(dir)
    }

    fun build(dir: Path): BuildScriptBuilder {
        return BuildScriptBuilderImpl(dir)
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

    private class MethodImpl(val text: String) : BlockElement {
        override fun renderContents(writer: PrintWriter, prefix: String) {
            writer.print(prefix)
            writer.println(text)
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

        override fun method(text: String) {
            elements.add(MethodImpl(text))
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

    private inner class SettingsScriptBuilderImpl(val dir: Path) : HasBlockContents(), SettingsScriptBuilder {
        private val includedBuilds = mutableListOf<String>()

        override fun includeBuild(path: String) {
            includedBuilds.add(path)
        }

        override fun complete() {
            textFileGenerator.file(dir.resolve("settings.${dsl.extension}")) {
                if (includedBuilds.isNotEmpty()) {
                    for (path in includedBuilds) {
                        println("includeBuild(\"$path\")")
                    }
                }
                renderElements(this, "")
            }
        }
    }

    private inner class BuildScriptBuilderImpl(val dir: Path) : HasBlockContents(), BuildScriptBuilder {
        private val plugins = mutableListOf<String>()

        override fun plugin(id: String) {
            plugins.add(id)
        }

        override fun complete() {
            textFileGenerator.file(dir.resolve("build.${dsl.extension}")) {
                if (plugins.isNotEmpty()) {
                    println()
                    println("plugins {")
                    for (plugin in plugins) {
                        println("    id(\"$plugin\")")
                    }
                    println("}")
                }
                renderElements(this, "")
            }
        }
    }
}