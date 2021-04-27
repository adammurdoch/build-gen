package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.CustomPluginSpecFactory
import net.rubygrapefruit.gen.builders.JavaConventionPluginSpecFactory
import net.rubygrapefruit.gen.builders.NoPluginSpecFactory
import net.rubygrapefruit.gen.builders.PluginSpecFactory

enum class Implementation(
    val pluginSpecFactory: PluginSpecFactory
) {
    None(NoPluginSpecFactory()), Custom(CustomPluginSpecFactory()), Java(JavaConventionPluginSpecFactory())
}