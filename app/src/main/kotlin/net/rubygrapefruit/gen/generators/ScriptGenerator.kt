package net.rubygrapefruit.gen.generators

import java.io.PrintWriter
import java.nio.file.Path

class ScriptGenerator {
    fun settings(file: Path) {
        script(file) {
        }
    }

    fun build(file: Path, body: BuildScriptBuilder.() -> Unit) {
        script(file) {
            println()
            println("plugins {")
            body(BuildScriptBuilderImpl(this))
            println("}")
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

    private class BuildScriptBuilderImpl(val writer: PrintWriter) : BuildScriptBuilder {
        override fun plugin(id: String) {
            writer.println("    id(\"$id\")")
        }
    }
}