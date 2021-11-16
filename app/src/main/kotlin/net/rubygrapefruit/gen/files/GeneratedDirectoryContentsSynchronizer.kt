package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.store.GenerationStateStore
import net.rubygrapefruit.gen.templates.Parameters
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class GeneratedDirectoryContentsSynchronizer(
    private val rootDir: Path
) {
    private val stateFile = GenerationStateStore(rootDir)

    fun isGenerated(): Boolean {
        return stateFile.exists
    }

    fun loadParameters(): Map<String, String> {
        return stateFile.load().options
    }

    fun sync(parameters: Parameters, action: (FileGenerationContext) -> Unit) {
        Files.createDirectories(rootDir)
        val previous = stateFile.load()
        val previousFiles = previous.files.toMutableSet()
        stateFile.storing(parameters) { writer ->
            val context = ContextImpl(rootDir, writer, previousFiles)
            action(context)
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

    private class ContextImpl(val rootDir: Path, val writer: GenerationStateStore.Writer, val previous: MutableSet<Path>) : FileGenerationContext {
        override fun generated(file: Path) {
            require(file.startsWith(rootDir))
            synchronized(this) {
                writer.generated(file)
                previous.remove(file)
            }
        }

        override fun directoryToClean(dir: Path) {
            require(dir.startsWith(rootDir))
            writer.directoryToClean(dir)
        }
    }
}