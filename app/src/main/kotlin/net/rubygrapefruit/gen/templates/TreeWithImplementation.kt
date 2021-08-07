package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.files.DslLanguage

class TreeWithImplementation(
    val treeTemplate: BuildTreeTemplate,
    val implementation: Implementation
) {
    val dslOptions = DslLanguage.values().toList()
    val themeOptions = Theme.values().toList()

    override fun toString(): String {
        return implementation.toString()
    }
}