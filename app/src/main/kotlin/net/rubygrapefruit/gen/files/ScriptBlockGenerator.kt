package net.rubygrapefruit.gen.files

interface ScriptBlockGenerator {
    /**
     * Assign a value to an eager property.
     */
    fun property(name: String, value: String)

    /**
     * Assing a value to a lazy property.
     */
    fun lazyProperty(name: String, value: String)

    fun block(name: String, body: ScriptBlockGenerator.() -> Unit = {})

    /**
     * An item in a named container.
     */
    fun namedItem(name: String, body: ScriptBlockGenerator.() -> Unit = {})

    fun method(text: String)
}