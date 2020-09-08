package net.rubygrapefruit.gen.generators

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path

class GeneratedDirectoryContentsSynchronizer {
    fun sync(rootDir: Path, action: (FileGenerationContext) -> Unit) {
        Files.createDirectories(rootDir)
        val file = rootDir.resolve("generation-state.txt")
        file.toFile().bufferedWriter().use {
            val writer = PrintWriter(it)
            writer.println("[generated files]")
            action(ContextImpl(rootDir, writer))
            writer.flush()
        }
    }

    private class ContextImpl(val rootDir: Path, val writer: PrintWriter) : FileGenerationContext {
        override fun generated(file: Path) {
            writer.println(rootDir.relativize(file).toString())
        }
    }
}