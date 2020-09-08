package net.rubygrapefruit.gen.specs

import net.rubygrapefruit.gen.PluginSpec
import java.nio.file.Path

interface BuildSpec {
    val displayName: String
    val rootDir: Path
    val childBuilds: List<BuildSpec>
    val requiresPlugins: List<PluginSpec>
    val producesPlugins: List<PluginSpec>
}