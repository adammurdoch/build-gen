package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

class ApplicationBuilder(
    private val projectNames: NameProvider,
    private var applicationSpecFactory: ApplicationSpecFactory
) : SingleComponentBuilder<AppProductionSpec>() {
    fun withFactory(applicationSpecFactory: ApplicationSpecFactory) {
        assertCanMutate()
        this.applicationSpecFactory = applicationSpecFactory
    }

    override fun createComponent(
        plugins: List<PluginUseSpec>,
        externalLibraries: List<ExternalLibraryProductionSpec>,
        internalLibraries: List<InternalLibraryProductionSpec>,
        incomingLibraries: List<ExternalLibraryUseSpec>
    ): AppProductionSpec {
        val baseName = BaseName(projectNames.next())
        val spec = applicationSpecFactory.application(baseName)
        return AppProductionSpec(baseName, spec, plugins, incomingLibraries, externalLibraries, internalLibraries)
    }
}