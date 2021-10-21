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

    fun method(signature: String, statements: Statements.() -> Unit)

    fun complete()

    interface Statements {
        fun statements(text: String)
        fun methodCall(text: String)
        fun variableDefinition(type: String, name: String, initializer: String?)
        fun ifStatement(condition: String, builder: Statements.() -> Unit)
    }
}