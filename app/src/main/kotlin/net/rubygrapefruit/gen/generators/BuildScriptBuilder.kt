package net.rubygrapefruit.gen.generators

interface BuildScriptBuilder: ScriptBlockGenerator {
    fun plugin(id: String)
}