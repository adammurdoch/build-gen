package net.rubygrapefruit.gen.files

import kotlin.reflect.KClass

interface PluginSourceBuilder {
    fun imports(name: String)

    fun imports(type: KClass<*>)

    fun applyMethodBody(text: String)

    val taskMethodContent: String

    fun taskMethodBody(text: String)
}