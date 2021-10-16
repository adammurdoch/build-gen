package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.BuildTreeBuilder

enum class TemplateOption(private val displayName: String) {
    ConfigurationCacheProblems("Configuration cache problems") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.includeConfigurationCacheProblems = true
        }
    },
    LargeBuild("Large build") {
        override fun applyTo(builder: BuildTreeBuilder) {
            TODO("Not yet implemented")
        }
    };

    override fun toString(): String = displayName

    abstract fun applyTo(builder: BuildTreeBuilder)
}