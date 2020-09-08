package net.rubygrapefruit.gen.files

interface ScriptBlockGenerator {
    fun property(name: String, value: String)

    fun block(name: String, body: ScriptBlockGenerator.() -> Unit = {})

    /**
     * An item in a named container.
     */
    fun namedItem(name: String, body: ScriptBlockGenerator.() -> Unit = {})

    fun method(text: String)
}