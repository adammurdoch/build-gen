package net.rubygrapefruit.gen.generators

import java.nio.file.Path

interface FileGenerationContext {
    fun generated(file: Path)
}