package net.rubygrapefruit.gen.files

import java.io.IOException
import java.io.PrintWriter
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.CopyOnWriteArraySet

private const val filesHeader = "[generated files]"
private const val dirsHeader = "[directories to clean]"

class GeneratedDirectoryContentsSynchronizer {
    fun sync(rootDir: Path, action: (FileGenerationContext) -> Unit) {
        Files.createDirectories(rootDir)
        val stateFile = rootDir.resolve("generation-state.txt")
        val previous = previousFiles(stateFile, rootDir)
        val previousFiles = previous.files.toMutableSet()
        stateFile.toFile().bufferedWriter().use {
            val writer = PrintWriter(it)
            writer.println(filesHeader)
            val context = ContextImpl(rootDir, writer, previousFiles)
            action(context)
            context.flush()
        }

        for (dir in previous.dirs) {
            require(dir.startsWith(rootDir))
            if (Files.exists(dir)) {
                println("* remove $dir/")
                removeDir(dir)
            }
        }

        for (file in previousFiles) {
            require(file.startsWith(rootDir))
            println("* remove $file")
            Files.deleteIfExists(file)
            val parent = file.parent
            removeIfEmpty(parent, rootDir)
        }
    }

    private fun removeIfEmpty(dir: Path?, rootDir: Path) {
        if (dir == null || dir == rootDir) {
            return
        }
        if (Files.list(dir).count() == 0.toLong()) {
            println("* remove $dir/")
            require(dir != rootDir)
            Files.delete(dir)
            removeIfEmpty(dir.parent, rootDir)
        }
    }

    private fun removeDir(dir: Path) {
        Files.walkFileTree(dir, object : FileVisitor<Path> {
            override fun preVisitDirectory(dir: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                return FileVisitResult.CONTINUE
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                Files.delete(file)
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path?, exc: IOException): FileVisitResult {
                throw exc
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                Files.delete(dir)
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun previousFiles(stateFile: Path, rootDir: Path): PreviousState {
        return if (Files.exists(stateFile)) {
            val lines = stateFile.toFile().readLines()
            val filesHeaderPos = lines.indexOf(filesHeader)
            val dirsHeaderPos = lines.indexOf(dirsHeader)
            if (filesHeaderPos != 0 || dirsHeaderPos < 0) {
                println("* discarding previous state")
                PreviousState(emptyList(), emptyList())
            } else {
                val files = lines.subList(1, dirsHeaderPos).map { rootDir.resolve(it) }
                val dirs = lines.drop(dirsHeaderPos + 1).map { rootDir.resolve(it) }
                PreviousState(files, dirs)
            }
        } else {
            PreviousState(emptyList(), emptyList())
        }
    }

    private class PreviousState(val files: List<Path>, val dirs: List<Path>)

    private class ContextImpl(val rootDir: Path, val writer: PrintWriter, val previous: MutableSet<Path>) : FileGenerationContext {
        private val dirsToClean = CopyOnWriteArraySet<Path>()

        override fun generated(file: Path) {
            synchronized(this) {
                writer.println(rootDir.relativize(file).toString())
                previous.remove(file)
            }
        }

        override fun directoryToClean(dir: Path) {
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