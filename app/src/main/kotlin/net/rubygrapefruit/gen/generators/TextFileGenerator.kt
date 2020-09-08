package net.rubygrapefruit.gen.generators

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path

class TextFileGenerator {
    fun file(file: Path, body: PrintWriter.() -> Unit) {
        Files.createDirectories(file.parent)
        file.toFile().bufferedWriter().use {
            PrintWriter(it).apply {
                println("// GENERATED FILE")
                body(this)
                println()
            }.also { it.flush() }
        }
    }
}