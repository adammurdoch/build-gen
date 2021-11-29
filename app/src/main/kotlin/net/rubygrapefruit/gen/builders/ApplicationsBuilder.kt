package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.AppProductionSpec
import net.rubygrapefruit.gen.specs.NameProvider

class ApplicationsBuilder(
    private val projectNames: NameProvider,
    private val applicationSpecFactory: ApplicationSpecFactory
) : MultipleComponentsBuilder<AppProductionSpec, ApplicationBuilder>() {
    override fun createBuilder(): ApplicationBuilder {
        return ApplicationBuilder(projectNames, applicationSpecFactory)
    }
}