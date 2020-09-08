package net.rubygrapefruit.gen.generators

interface JavaSourceFileBuilder {
    fun imports(name: String)

    fun implements(name: String)

    fun method(text: String)
}