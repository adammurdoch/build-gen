package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.specs.PluginImplementationSpec

interface PluginImplementationBuilder {
    val spec: PluginImplementationSpec
    val source: PluginSourceBuilder
    val includeConfigurationCacheProblems: Boolean
}