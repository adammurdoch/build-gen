package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.extensions.capitalized
import net.rubygrapefruit.gen.specs.JvmClassName
import kotlin.reflect.KClass

sealed class JvmType {
    abstract val typeDeclaration: String

    abstract val asVarargs: JvmType

    abstract fun newInstance(vararg params: RValue): Expression

    fun newInstance(stringLiteral: String): Expression = newInstance(stringLiteral.expression)

    abstract fun visitTypes(consumer: (JvmClassName) -> Unit)

    companion object {
        val voidType = RawType(JvmClassName("void"))
        val longType = RawType(JvmClassName("long"))
        val stringType = type(String::class)

        fun type(name: String): RawType = RawType(JvmClassName(name))
        fun type(name: JvmClassName): RawType = RawType(name)
        fun type(rawType: KClass<*>): RawType = RawType(javaType(rawType))
        fun type(name: String, param: String): JvmType = ParameterizedType(JvmClassName(name), type(param))
        fun type(name: String, param: JvmClassName): JvmType = ParameterizedType(JvmClassName(name), type(param))
        fun type(name: String, param: JvmType): JvmType = ParameterizedType(JvmClassName(name), param)
        fun type(rawType: KClass<*>, param: JvmClassName): JvmType = ParameterizedType(javaType(rawType), type(param))
        fun type(name: String, param: KClass<*>): JvmType = ParameterizedType(JvmClassName(name), type(param))
        fun type(rawType: KClass<*>, param: KClass<*>): JvmType = ParameterizedType(javaType(rawType), type(param))
        fun type(rawType: KClass<*>, param: JvmType): JvmType = ParameterizedType(javaType(rawType), param)

        private fun javaType(type: KClass<*>): JvmClassName {
            if (type == String::class) {
                return JvmClassName(String::class.java.name)
            } else if (type == Set::class) {
                return JvmClassName(Set::class.java.name)
            } else if (type == List::class) {
                return JvmClassName(List::class.java.name)
            } else if (type == LinkedHashSet::class) {
                return JvmClassName(LinkedHashSet::class.java.name)
            } else {
                return JvmClassName(type.qualifiedName!!)
            }
        }
    }
}

class RawType(
    val name: JvmClassName
) : JvmType() {
    override val typeDeclaration: String
        get() = name.simpleName

    override val asVarargs: JvmType
        get() = VarargsType(this)

    val asTypeLiteral
        get() = Expression("${name.simpleName}.class")

    override fun newInstance(vararg params: RValue) = Expression("new ${name.simpleName}(${params.joinToString(", ") { it.literal }})")

    override fun visitTypes(consumer: (JvmClassName) -> Unit) {
        consumer(name)
    }

    fun field(name: String) = Expression("${this.name.simpleName}.$name")

    fun staticMethodCall(name: String) = Expression("${this.name.simpleName}.$name()")
}

class ParameterizedType(
    val rawType: JvmClassName,
    val param: JvmType
) : JvmType() {
    override val typeDeclaration: String
        get() = "${rawType.simpleName}<${param.typeDeclaration}>"

    override val asVarargs: JvmType
        get() = VarargsType(RawType(rawType))

    override fun newInstance(vararg params: RValue) = Expression("new ${rawType.simpleName}<>(${params.joinToString(", ") { it.literal }})")

    override fun visitTypes(consumer: (JvmClassName) -> Unit) {
        consumer(rawType)
        param.visitTypes(consumer)
    }
}

class VarargsType(
    val type: JvmType
) : JvmType() {
    override val typeDeclaration: String
        get() = "${type.typeDeclaration}..."

    override val asVarargs: JvmType
        get() = throw UnsupportedOperationException()

    override fun newInstance(vararg params: RValue) = throw UnsupportedOperationException()

    override fun visitTypes(consumer: (JvmClassName) -> Unit) {
        type.visitTypes(consumer)
    }
}

sealed class RValue {
    abstract val literal: String

    operator fun plus(other: RValue) = Expression("($literal + ${other.literal})")

    operator fun minus(other: RValue) = Expression("($literal - ${other.literal})")

    operator fun div(other: RValue) = Expression("($literal / ${other.literal})")
}

sealed class InstanceRef(
    val name: String,
    val type: JvmType
) : RValue() {
    override val literal = name

    fun methodCall(name: String, vararg params: RValue): Expression {
        return Expression("${this.name}.$name(${params.joinToString(", ") { it.literal }})")
    }

    fun readProperty(name: String): Expression {
        return Expression("${this.name}.get${name.capitalized()}()")
    }
}

class LocalVariable(
    name: String,
    type: JvmType
) : InstanceRef(name, type)

class InstanceVariable(
    name: String,
    type: JvmType
) : InstanceRef(name, type)

class Expression(
    override val literal: String
) : RValue() {
    companion object {
        fun string(stringLiteral: String): Expression = Expression("\"$stringLiteral\"")
        fun longValue(value: Long): Expression = Expression("${value}")
    }
}

val String.expression: Expression
    get() = Expression.string(this)

val Long.expression: Expression
    get() = Expression.longValue(this)
