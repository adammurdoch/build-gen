package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.ProductionBuildTreeBuilder

abstract class TemplateOption(private val displayName: String) {
    companion object {
        val configurationCacheProblems = object : TemplateOption("Configuration cache problems") {
            override fun ProductionBuildTreeBuilder.applyTo() {
                includeConfigurationCacheProblems()
            }
        }
        val largeBuild = object : TemplateOption("Large build (10 projects)") {
            override fun ProductionBuildTreeBuilder.applyTo() {
                main.includeComponents(10)
            }
        };
    }

    override fun toString(): String = displayName

    abstract fun ProductionBuildTreeBuilder.applyTo()
}