package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.specs.JvmClassName
import kotlin.reflect.KClass

interface JavaSourceFileBuilder {
    fun imports(type: KClass<*>) {
        imports(type.java.name)
    }

    fun imports(name: String)

    fun imports(name: JvmClassName)

    fun extends(type: JvmType)

    fun implements(type: JvmType)

    /**
     * Adds a property and constructor that initialises it
     */
    fun constructorAndProperty(name: String, type: JvmType): InstanceVariable

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

        fun thisMethodCall(name: String, vararg parameters: RValue)

        fun staticMethodCall(target: RawType, name: String, vararg parameters: RValue)

        fun methodCall(target: LocalVariable, name: String, vararg parameters: RValue)
        fun methodCall(target: LocalVariable, name: String, stringLiteral: String) = methodCall(target, name, stringLiteral.expression)

        fun log(expression: RValue)
        fun log(text: String) = log(text.expression)
        fun log(text: String, expression: RValue) = log(text.expression + expression)

        fun variableDefinition(type: JvmType, name: String, initializer: RValue?): LocalVariable

        fun ifStatement(condition: RValue, builder: Statements.() -> Unit)

        fun iterate(type: JvmType, itemName: String, valuesExpression: RValue, builder: Statements.(LocalVariable) -> Unit)

        fun returnValue(expression: RValue)
        fun returnValue(stringLiteral: String) = returnValue(stringLiteral.expression)
    }
}