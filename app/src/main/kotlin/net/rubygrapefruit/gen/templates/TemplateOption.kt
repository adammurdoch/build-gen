package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.ProductionBuildTreeBuilder

abstract class TemplateOption(val displayName: String) {
    companion object {
        val configurationCacheProblems = object : TemplateOption("Configuration cache problems") {
            override fun ProductionBuildTreeBuilder.applyTo() {
                includeConfigurationCacheProblems()
            }
        }

        private val largeBuildProjects = 500
        val largeBuild = object : TemplateOption("Large build ($largeBuildProjects projects)") {
            override fun ProductionBuildTreeBuilder.applyTo() {
                main.includeComponents(largeBuildProjects)
            }
        }

        val toolingApiClient = object : TemplateOption("Tooling API client") {
            override fun ProductionBuildTreeBuilder.applyTo() {
                toolingApiClient()
            }
        }
    }

    override fun toString(): String = displayName

    abstract fun ProductionBuildTreeBuilder.applyTo()
}