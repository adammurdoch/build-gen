package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.PluginSourceBuilder
import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.PluginProductionSpec

interface PluginGenerationContext {
    val build: BuildSpec
    val spec: PluginProductionSpec
    val source: PluginSourceBuilder
}