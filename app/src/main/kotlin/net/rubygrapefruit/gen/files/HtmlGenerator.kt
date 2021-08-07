package net.rubygrapefruit.gen.files

import java.io.PrintWriter
import java.nio.file.Path

class HtmlGenerator(
    val textFileGenerator: TextFileGenerator
) {
    fun file(file: Path, body: PrintWriter.() -> Unit) {
        textFileGenerator.file(file, body)
    }
}