package net.rubygrapefruit.gen.files

interface PluginSourceBuilder {
    fun applyMethodBody(text: String)

    fun taskMethodBody(text: String)
}