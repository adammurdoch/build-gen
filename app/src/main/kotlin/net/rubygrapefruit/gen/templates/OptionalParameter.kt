package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.files.DslLanguage

sealed class OptionalParameter<T : Any>(
    val displayName: String
) {
    override fun toString() = displayName

    abstract val defaultValue: T
}

class EnumParameter<T : Enum<T>>(
    displayName: String,
    val candidates: List<T>
) : OptionalParameter<T>(displayName) {
    override val defaultValue: T
        get() = candidates.first()
}

val dslParameter = EnumParameter("DSL language", DslLanguage.values().toList())

class BooleanParameter(
    val templateOption: TemplateOption
) : OptionalParameter<Boolean>(templateOption.displayName) {
    override val defaultValue: Boolean
        get() = false
}
