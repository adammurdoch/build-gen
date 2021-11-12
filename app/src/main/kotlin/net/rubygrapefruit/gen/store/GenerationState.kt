package net.rubygrapefruit.gen.store

import java.nio.file.Path

class GenerationState(
    val options: Map<String, String>?,
    val files: List<Path>,
    val dirs: List<Path>
)