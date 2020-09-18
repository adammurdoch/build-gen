package net.rubygrapefruit.gen.files

interface SettingsScriptBuilder : ScriptBlockGenerator {
    fun includeProject(path: String)

    fun includeBuild(path: String)

    fun complete()
}