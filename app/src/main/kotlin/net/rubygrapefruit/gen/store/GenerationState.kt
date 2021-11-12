package net.rubygrapefruit.gen.store

import java.nio.file.Path

class GenerationState(
    val files: List<Path>,
    val dirs: List<Path>
)