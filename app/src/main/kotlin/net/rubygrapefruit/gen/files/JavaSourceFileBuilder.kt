package net.rubygrapefruit.gen.files

import kotlin.reflect.KClass

interface JavaSourceFileBuilder {
    fun imports(type: KClass<*>) {
        imports(type.java.name)
    }

    fun imports(name: String)

    fun extends(name: String)

    fun implements(name: String)

    fun abstractMethod(text: String)

    fun method(text: String)

    fun complete()
}