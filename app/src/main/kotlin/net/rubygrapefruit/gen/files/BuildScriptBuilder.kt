package net.rubygrapefruit.gen.files

interface BuildScriptBuilder: ScriptBlockGenerator {
    fun plugin(id: String)

    fun implementationDependency(projectPath: String)

    fun complete()
}