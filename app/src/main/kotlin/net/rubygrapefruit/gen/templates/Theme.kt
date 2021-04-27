package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class Theme(private val displayName: String) {
    None("None") {
        override fun applyTo(builder: BuildTreeBuilder) {
        }
    },
    ConfigurationCacheProblems("Configuration cache problems") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.includeConfigurationCacheProblems = true
        }
    };

    override fun toString(): String = displayName

    abstract fun applyTo(builder: BuildTreeBuilder)
}