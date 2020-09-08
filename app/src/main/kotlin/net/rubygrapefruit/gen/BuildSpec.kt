package net.rubygrapefruit.gen

interface BuildSpec {
    val displayName: String
    val rootDir: String
    val requiresPlugins: List<PluginSpec>
    val producesPlugins: List<PluginSpec>
}