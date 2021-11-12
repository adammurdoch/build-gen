package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.files.DslLanguage

sealed class OptionalParameter<T : Any>(
    val displayName: String
) {
    override fun toString() = displayName

    abstract fun apply(parameters: Parameters, value: T): Parameters
}

sealed class EnumParameter<T : Enum<T>>(
    displayName: String,
    val candidates: List<T>
) : OptionalParameter<T>(displayName) {
    abstract fun value(parameters: Parameters): T
}

object dslParameter : EnumParameter<DslLanguage>("DSL language", DslLanguage.values().toList()) {
    override fun apply(parameters: Parameters, value: DslLanguage) = parameters.withDslLanguage(value)

    override fun value(parameters: Parameters) = parameters.dsl
}

class BooleanParameter(
    val templateOption: TemplateOption
) : OptionalParameter<Boolean>(templateOption.displayName) {
    override fun apply(parameters: Parameters, value: Boolean): Parameters {
        return parameters.withOption(templateOption, value)
    }
}
