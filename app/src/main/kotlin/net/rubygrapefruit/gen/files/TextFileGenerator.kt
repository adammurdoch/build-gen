package net.rubygrapefruit.gen.files

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path

class TextFileGenerator(private val listener: FileGenerationContext) {
    fun file(file: Path, body: PrintWriter.() -> Unit) {
        Files.createDirectories(file.parent)
        file.toFile().bufferedWriter().use {
            PrintWriter(it).apply {
                body(this)
                println()
            }.also { it.flush() }
        }
        listener.generated(file)
    }
}