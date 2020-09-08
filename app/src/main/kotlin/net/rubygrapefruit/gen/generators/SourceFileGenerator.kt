package net.rubygrapefruit.gen.generators

import java.nio.file.Path

class SourceFileGenerator(private val textFileGenerator: TextFileGenerator) {
    fun java(srcDir: Path, className: String, body: SourceFileBuilder.() -> Unit) {
        textFileGenerator.file(srcDir.resolve(className.replace(".", "/") + ".java")) {
        }
    }
}