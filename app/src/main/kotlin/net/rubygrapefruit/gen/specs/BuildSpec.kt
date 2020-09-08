package net.rubygrapefruit.gen.specs

import net.rubygrapefruit.gen.PluginSpec
import java.nio.file.Path

interface BuildSpec {
    val displayName: String
    val rootDir: Path
    val requiresPlugins: List<PluginSpec>
    val producesPlugins: List<PluginSpec>
}