package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.files.DslLanguage

class Parameters(
    val treeTemplate: BuildTreeTemplate,
    val implementation: Implementation,
    val availableOptions: List<TemplateOption>,
    val enabledOptions: List<TemplateOption>
) {
    val dslOptions = DslLanguage.values().toList()

    override fun toString(): String {
        return implementation.toString()
    }

    fun enable(templateOption: TemplateOption): Parameters {
        return Parameters(treeTemplate, implementation, availableOptions, enabledOptions + templateOption)
    }

    fun disable(templateOption: TemplateOption): Parameters {
        return Parameters(treeTemplate, implementation, availableOptions, enabledOptions.filter { it != templateOption })
    }
}