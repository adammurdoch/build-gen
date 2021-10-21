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

    /**
     * public void method by default
     */
    fun method(name: String, builder: MethodBuilder.() -> Unit)

    /**
     * public void method by default
     */
    fun method(name: String, param1: String, paramType1: JvmType, builder: MethodBuilder.(LocalVariable) -> Unit)

    /**
     * public void method by default
     */
    fun method(name: String, param1: String, paramType1: JvmType, param2: String, paramType2: JvmType, builder: MethodBuilder.(LocalVariable, LocalVariable) -> Unit)

    /**
     * public static void method by default
     */
    fun staticMethod(name: String, param1: String, paramType1: JvmType, builder: MethodBuilder.(LocalVariable) -> Unit)

    interface MethodBuilder {
        fun private()
        fun annotation(type: RawType)
        fun returnType(type: JvmType)
        fun throwsException(type: JvmType)
        fun body(builder: Statements.() -> Unit)
    }

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