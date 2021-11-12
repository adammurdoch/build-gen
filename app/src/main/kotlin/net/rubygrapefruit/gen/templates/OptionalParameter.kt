package net.rubygrapefruit.gen.templates

sealed class OptionalParameter<T: Any>(
    val displayName: String
) {
    override fun toString() = displayName

    abstract val defaultValue: T
}

class EnumParameter<T: Enum<T>>(
    displayName: String,
    val candidates: List<T>
) : OptionalParameter<T>(displayName) {
    override val defaultValue: T
        get() = candidates.first()
}

class BooleanParameter(
    val templateOption: TemplateOption
) : OptionalParameter<Boolean>(templateOption.displayName) {
    override val defaultValue: Boolean
        get() = false
}
