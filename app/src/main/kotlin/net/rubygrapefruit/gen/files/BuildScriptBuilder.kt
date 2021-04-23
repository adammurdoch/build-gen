package net.rubygrapefruit.gen.files

interface BuildScriptBuilder : ScriptBlockGenerator {
    fun plugin(id: String)

    fun implementationDependency(projectPath: String)

    fun implementationDependency(group: String, name: String, version: String)

    fun group(group: String)

    fun complete()
}