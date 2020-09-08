package net.rubygrapefruit.gen.files

import java.nio.file.Path

interface FileGenerationContext {
    fun generated(file: Path)
}