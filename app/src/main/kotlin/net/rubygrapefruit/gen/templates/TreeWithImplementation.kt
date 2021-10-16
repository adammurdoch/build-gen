package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.files.DslLanguage

class TreeWithImplementation(
    val treeTemplate: BuildTreeTemplate,
    val implementation: Implementation,
    val availableOptions: List<TemplateOption>,
    val enabledOptions: List<TemplateOption>
) {
    val dslOptions = DslLanguage.values().toList()

    override fun toString(): String {
        return implementation.toString()
    }

    fun enable(templateOption: TemplateOption): TreeWithImplementation {
        return TreeWithImplementation(treeTemplate, implementation, availableOptions, enabledOptions + templateOption)
    }

    fun disable(templateOption: TemplateOption): TreeWithImplementation {
        return TreeWithImplementation(treeTemplate, implementation, availableOptions, enabledOptions.filter { it != templateOption })
    }
}