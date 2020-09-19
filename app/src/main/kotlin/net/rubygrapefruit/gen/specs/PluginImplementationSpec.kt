package net.rubygrapefruit.gen.specs

class PluginImplementationSpec(
        val project: ProjectSpec,
        val spec: PluginProductionSpec,
        val pluginImplementationClass: JvmClassName,
        val taskImplementationClass: JvmClassName
)