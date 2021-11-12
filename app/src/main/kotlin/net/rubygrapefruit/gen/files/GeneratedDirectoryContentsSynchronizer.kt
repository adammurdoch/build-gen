package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.store.GenerationStateStore
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

class GeneratedDirectoryContentsSynchronizer {
    fun isGenerated(rootDir: Path): Boolean {
        return GenerationStateStore(rootDir).exists
    }

    fun sync(rootDir: Path, action: (FileGenerationContext) -> Unit) {
        Files.createDirectories(rootDir)
        val stateFile = GenerationStateStore(rootDir)
        val previous = stateFile.load()
        val previousFiles = previous.files.toMutableSet()
        stateFile.storing { writer ->
            val context = ContextImpl(writer, previousFiles)
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

    private class ContextImpl(val writer: GenerationStateStore.Writer, val previous: MutableSet<Path>) : FileGenerationContext {
        override fun generated(file: Path) {
            synchronized(this) {
                writer.generated(file)
                previous.remove(file)
            }
        }

        override fun directoryToClean(dir: Path) {
            writer.directoryToClean(dir)
        }
    }
}