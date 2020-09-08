package net.rubygrapefruit.gen.generators

import java.io.PrintWriter
import java.nio.file.Path

class ScriptGenerator {
    fun settings(file: Path, body: SettingsScriptBuilder.() -> Unit) {
        script(file) {
            SettingsScriptBuilderImpl(this).run(body)
        }
    }

    fun build(file: Path, body: BuildScriptBuilder.() -> Unit) {
        script(file) {
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

    private class BuildScriptBuilderImpl(val writer: PrintWriter) : BuildScriptBuilder {
        private var pluginsBlock = false

        override fun plugin(id: String) {
            if (!pluginsBlock) {
                writer.println()
                writer.println("plugins {")
                pluginsBlock = true
            }
            writer.println("    id(\"$id\")")
        }

        fun run(body: BuildScriptBuilder.() -> Unit) {
            body(this)
            if (pluginsBlock) {
                writer.println("}")
            }
        }
    }
}