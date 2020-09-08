package net.rubygrapefruit.gen.files

interface SettingsScriptBuilder : ScriptBlockGenerator {
    fun includeBuild(path: String)

    fun complete()
}