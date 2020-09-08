package net.rubygrapefruit.gen.generators

interface ScriptBlockGenerator {
    fun property(name: String, value: String)

    fun block(name: String, body: ScriptBlockGenerator.() -> Unit)
}