package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.PluginUseSpec

interface BuildRelationshipBuilder {
    fun requires(plugin: PluginUseSpec)
}