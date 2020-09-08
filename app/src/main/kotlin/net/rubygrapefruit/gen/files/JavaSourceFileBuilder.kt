package net.rubygrapefruit.gen.files

interface JavaSourceFileBuilder {
    fun imports(name: String)

    fun implements(name: String)

    fun method(text: String)

    fun complete()
}