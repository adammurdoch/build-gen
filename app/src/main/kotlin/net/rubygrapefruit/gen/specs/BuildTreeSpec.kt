package net.rubygrapefruit.gen.specs

import java.nio.file.Path

/**
 * Defines the build tree structure.
 */
interface BuildTreeSpec {
    val rootDir: Path
    val builds: List<BuildSpec>
    val heapSize: String?
}