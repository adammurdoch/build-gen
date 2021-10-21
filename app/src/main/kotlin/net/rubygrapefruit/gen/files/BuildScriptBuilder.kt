package net.rubygrapefruit.gen.files

import java.net.URI

interface BuildScriptBuilder : ScriptBlockGenerator {
    fun plugin(id: String)

    fun implementationDependency(projectPath: String)

    fun implementationDependency(group: String, name: String, version: String)

    fun repository(uri: URI)

    fun complete()
}