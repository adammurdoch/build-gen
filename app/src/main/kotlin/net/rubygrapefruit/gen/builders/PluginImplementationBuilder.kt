package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.specs.PluginProductionSpec
import net.rubygrapefruit.gen.specs.ProjectSpec

interface PluginImplementationBuilder {
    val spec: PluginProductionSpec
    val source: PluginSourceBuilder
    val includeConfigurationCacheProblems: Boolean
}