package net.rubygrapefruit.gen.store

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.io.path.exists

private const val parametersHeader = "[parameters]"
private const val filesHeader = "[generated files]"
private const val dirsHeader = "[directories to clean]"

class GenerationStateStore(
    private val rootDir: Path
) {
    private val stateFile: Path = rootDir.resolve("generation-state.txt")

    val exists: Boolean
        get() = stateFile.exists()

    fun load(): GenerationState {
        return if (Files.exists(stateFile)) {
            val lines = stateFile.toFile().readLines()
            val parametersHeaderPos = lines.indexOf(parametersHeader)
            val filesHeaderPos = lines.indexOf(filesHeader)
            val dirsHeaderPos = lines.indexOf(dirsHeader)
            if (parametersHeaderPos != 0 || filesHeaderPos < 0 || dirsHeaderPos < 0 || filesHeaderPos > dirsHeaderPos) {
                println("* discarding previous state")
                GenerationState(null, emptyList(), emptyList())
            } else {
                val parameters = lines.subList(parametersHeaderPos + 1, filesHeaderPos).map {
                    val p = it.split("=")
                    p[0] to p[1]
                }.toMap()
                val files = lines.subList(filesHeaderPos + 1, dirsHeaderPos).map { rootDir.resolve(it) }
                val dirs = lines.drop(dirsHeaderPos + 1).map { rootDir.resolve(it) }
                GenerationState(parameters, files, dirs)
            }
        } else {
            GenerationState(null, emptyList(), emptyList())
        }
    }

    fun storing(action: (Writer) -> Unit) {
        stateFile.toFile().bufferedWriter().use {
            val writer = PrintWriter(it)
            writer.println(parametersHeader)
            writer.println("param1=value1")
            writer.println("param2=value2")
            writer.println(filesHeader)
            val context = Writer(writer, rootDir)
            action(context)
            context.flush()
        }
    }

    class Writer(
        private val writer: PrintWriter,
        private val rootDir: Path
    ) {
        private val dirsToClean = CopyOnWriteArraySet<Path>()

        fun generated(file: Path) {
            synchronized(this) {
                writer.println(rootDir.relativize(file).toString())
            }
        }

        fun directoryToClean(dir: Path) {
            dirsToClean.add(dir)
        }

        fun flush() {
            writer.println(dirsHeader)
            for (dir in dirsToClean) {
                writer.println(rootDir.relativize(dir).toString())
            }
            writer.flush()
        }
    }
}