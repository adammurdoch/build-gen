package net.rubygrapefruit.gen.files

import kotlin.reflect.KClass

interface PluginSourceBuilder {
    fun imports(name: String)

    fun imports(type: KClass<*>)

    fun applyMethodBody(body: JavaSourceFileBuilder.Statements.() -> Unit)

    val taskMethodContent: String

    fun taskMethodBody(text: String)
}