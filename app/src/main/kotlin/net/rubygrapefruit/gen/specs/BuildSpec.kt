package net.rubygrapefruit.gen.specs

import java.nio.file.Path

interface BuildSpec {
    val displayName: String
    val rootDir: Path
    val childBuilds: List<BuildSpec>
    val usesPlugins: List<PluginUseSpec>
    val producesPlugins: List<PluginProductionSpec>
}