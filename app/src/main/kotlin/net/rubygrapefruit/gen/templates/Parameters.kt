package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.files.DslLanguage

class Parameters(
    val treeTemplate: BuildTreeTemplate,
    val implementation: Implementation,
    val availableOptions: List<OptionalParameter<*>>,
    private val optionValues: Map<Any, Any> = emptyMap()
) {
    override fun toString(): String {
        return implementation.toString()
    }

    val dsl: DslLanguage
        get() = value(dslParameter)

    val enabledOptions: List<TemplateOption>
        get() = optionValues.mapNotNull {
            val key = it.key
            if (key is BooleanParameter && it.value == true) key.templateOption else null
        }

    fun <T : Any> withValue(parameter: OptionalParameter<T>, value: T): Parameters {
        val newOptions: Map<Any, Any> = optionValues + mapOf((parameter as Any) to (value as Any))
        return Parameters(treeTemplate, implementation, availableOptions, newOptions)
    }

    fun <T : Any> value(parameter: OptionalParameter<T>): T {
        val value = optionValues[parameter]
        return if (value != null) {
            value as T
        } else {
            parameter.defaultValue
        }
    }
}