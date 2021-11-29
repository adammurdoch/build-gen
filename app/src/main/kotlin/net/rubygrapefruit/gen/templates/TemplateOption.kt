package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.ProductionBuildTreeBuilder

abstract class TemplateOption(
    val id: String,
    val displayName: String
) {
    companion object {
        val configurationCacheProblems = object : TemplateOption("cc-problems", "Configuration cache problems") {
            override fun ProductionBuildTreeBuilder.applyTo() {
                includeConfigurationCacheProblems()
            }
        }

        private const val largeBuildProjects = 20
        val largeBuild = object : TemplateOption("large-build", "Large build ($largeBuildProjects projects)") {
            override fun ProductionBuildTreeBuilder.applyTo() {
                main.includeComponents(largeBuildProjects)
            }
        }

        val toolingApiClient = object : TemplateOption("tooling-api-client", "Tooling API client") {
            override fun ProductionBuildTreeBuilder.applyTo() {
                toolingApiClient()
            }
        }
    }

    override fun toString(): String = displayName

    abstract fun ProductionBuildTreeBuilder.applyTo()
}