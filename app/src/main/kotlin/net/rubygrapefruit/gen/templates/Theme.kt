package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class Theme(private val displayName: String) {
    None("none") {
        override fun applyTo(builder: BuildTreeBuilder) {
        }
    },
    ConfigurationCacheProblems("configuration cache problems") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.includeConfigurationCacheProblems = true
        }
    };

    override fun toString(): String = displayName

    abstract fun applyTo(builder: BuildTreeBuilder)
}