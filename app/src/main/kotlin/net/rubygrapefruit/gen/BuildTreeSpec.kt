package net.rubygrapefruit.gen

import java.nio.file.Path

interface BuildTreeSpec {
    val rootDir: Path
    val builds: List<BuildSpec>
}