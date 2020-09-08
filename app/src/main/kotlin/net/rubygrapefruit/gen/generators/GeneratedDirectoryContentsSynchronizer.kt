package net.rubygrapefruit.gen.generators

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path

private const val header = "[generated files]"

class GeneratedDirectoryContentsSynchronizer {
    fun sync(rootDir: Path, action: (FileGenerationContext) -> Unit) {
        Files.createDirectories(rootDir)
        val stateFile = rootDir.resolve("generation-state.txt")
        val previous = previousFiles(stateFile, rootDir)
        stateFile.toFile().bufferedWriter().use {
            val writer = PrintWriter(it)
            writer.println(header)
            action(ContextImpl(rootDir, writer, previous))
            writer.flush()
        }

        for (file in previous) {
            require(file.startsWith(rootDir))
            println("* remove ${file}")
            Files.deleteIfExists(file)
        }
    }

    private fun previousFiles(stateFile: Path, rootDir: Path): MutableSet<Path> {
        return if (Files.exists(stateFile)) {
            val lines = stateFile.toFile().readLines()
            if (!lines.first().equals(header)) {
                println("* discarding previous state")
                mutableSetOf()
            } else {
                lines.drop(1).map { rootDir.resolve(it) }.toMutableSet()
            }
        } else {
            mutableSetOf()
        }
    }

    private class ContextImpl(val rootDir: Path, val writer: PrintWriter, val previous: MutableSet<Path>) : FileGenerationContext {
        override fun generated(file: Path) {
            writer.println(rootDir.relativize(file).toString())
            previous.remove(file)
        }
    }
}