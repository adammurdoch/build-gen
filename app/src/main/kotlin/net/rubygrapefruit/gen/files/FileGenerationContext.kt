package net.rubygrapefruit.gen.files

import java.nio.file.Path

interface FileGenerationContext {
    /**
     * Indicates that that given file has been generated and registers it for cleanup.
     */
    fun generated(file: Path)

    /**
     * Registers a working directory that should be removed on next generation.
     */
    fun directoryToClean(dir: Path)
}