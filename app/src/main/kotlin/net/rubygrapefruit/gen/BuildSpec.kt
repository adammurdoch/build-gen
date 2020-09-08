package net.rubygrapefruit.gen

import java.nio.file.Path

interface BuildSpec {
    val displayName: String
    val rootDir: Path
    val requiresPlugins: List<PluginSpec>
    val producesPlugins: List<PluginSpec>
}