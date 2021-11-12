package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.files.DslLanguage

class Parameters(
    val treeTemplate: BuildTreeTemplate,
    val implementation: Implementation,
    val availableOptions: List<OptionalParameter<*>>,
    val dsl: DslLanguage,
    val enabledOptions: Set<TemplateOption> = emptySet()
) {
    override fun toString(): String {
        return implementation.toString()
    }

    fun withOption(option: TemplateOption, enabled: Boolean): Parameters {
        val newOptions = if (enabled) enabledOptions + option else enabledOptions - option
        return Parameters(treeTemplate, implementation, availableOptions, dsl, newOptions)
    }

    fun enabled(option: TemplateOption) = enabledOptions.contains(option)

    fun withDslLanguage(dsl: DslLanguage): Parameters {
        return Parameters(treeTemplate, implementation, availableOptions, dsl, enabledOptions)
    }
}