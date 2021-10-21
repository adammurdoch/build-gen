package net.rubygrapefruit.gen.files

import net.rubygrapefruit.gen.specs.JvmClassName
import kotlin.reflect.KClass

sealed class JvmType {
    abstract fun newInstance(): Expression

    abstract val typeDeclaration: String

    companion object {
        fun type(name: String): RawType = RawType(JvmClassName(name))
        fun type(name: JvmClassName): RawType = RawType(name)
        fun type(name: String, param: String): JvmType = ParameterizedType(JvmClassName(name), JvmClassName(param))
        fun type(name: String, param: JvmClassName): JvmType = ParameterizedType(JvmClassName(name), param)
        fun type(rawType: KClass<*>, param: JvmClassName): JvmType = ParameterizedType(javaType(rawType), param)
        fun type(name: String, param: KClass<*>): JvmType = ParameterizedType(JvmClassName(name), javaType(param))
        fun type(rawType: KClass<*>, param: KClass<*>): JvmType = ParameterizedType(javaType(rawType), javaType(param))

        private fun javaType(type: KClass<*>): JvmClassName {
            if (type == String::class) {
                return JvmClassName(String::class.java.name)
            } else if (type == Set::class) {
                return JvmClassName(Set::class.java.name)
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

    override fun newInstance() = Expression("new ${name.simpleName}()")

    fun staticMethod(name: String) = Expression("${this.name.simpleName}.$name()")
}

class ParameterizedType(
    val name: JvmClassName,
    val param: JvmClassName
) : JvmType() {
    override val typeDeclaration: String
        get() = "${name.simpleName}<${param.simpleName}>"

    override fun newInstance() = Expression("new ${name.simpleName}<>()")
}

class LocalVariable(
    val name: String,
    val type: JvmType
) {
    fun methodCall(name: String, vararg params: Expression): Expression {
        return Expression("${this.name}.$name(${params.joinToString(", ") { it.literal }})")
    }

    fun readProperty(name: String): Expression {
        return Expression("${this.name}.get${name.capitalize()}()")
    }
}

class Expression(
    val literal: String
)