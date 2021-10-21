package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.specs.JvmClassName
import kotlin.reflect.KClass

interface JavaSourceFileBuilder {
    fun imports(type: KClass<*>) {
        imports(type.java.name)
    }

    fun imports(name: String)

    fun imports(name: JvmClassName)

    fun extends(name: String)

    fun implements(name: String)

    fun abstractMethod(text: String)

    fun method(text: String)

    fun method(signature: String, statements: Statements.() -> Unit)

    fun complete()

    interface Statements {
        fun statements(literals: String)
        fun methodCall(literal: String)
        fun log(text: String)
        fun variableDefinition(type: JvmType, name: String, initializer: Expression?): LocalVariable
        fun ifStatement(condition: String, builder: Statements.() -> Unit)
        fun iterate(type: String, itemName: String, valuesExpression: String, builder: Statements.() -> Unit)
        fun returnValue(expression: String)
    }
}