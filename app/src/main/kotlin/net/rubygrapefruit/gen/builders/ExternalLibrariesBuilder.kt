package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

class ExternalLibrariesBuilder(
    private val projectNames: NameProvider,
    private val group: String,
    private val librarySpecFactory: LibrarySpecFactory,
) : AbstractBuildComponentsBuilder<ExternalLibraryProductionSpec>() {
    val exportedLibraries: ExternalLibrariesSpec = object : ExternalLibrariesSpec {
        override val libraries: List<ExternalLibraryProductionSpec>
            get() = contents
    }

    val useSpec: IncomingLibrariesSpec = object : IncomingLibrariesSpec {
        override val libraries: List<ExternalLibraryUseSpec>
            get() = contents.map { ExternalLibraryUseSpec(it.coordinates, it.spec.toApiSpec()) }
    }

    override fun createComponent(
        plugins: List<PluginUseSpec>,
        externalLibraries: List<ExternalLibraryProductionSpec>,
        internalLibraries: List<InternalLibraryProductionSpec>,
        incomingLibraries: List<ExternalLibraryUseSpec>
    ): ExternalLibraryProductionSpec {
        val name = BaseName(projectNames.next())
        val coordinates = ExternalLibraryCoordinates(group, name.camelCase, "1.0")
        val libraryApi = librarySpecFactory.library(name)
        return ExternalLibraryProductionSpec(
            coordinates,
            libraryApi,
            plugins,
            incomingLibraries,
            externalLibraries,
            internalLibraries
        )
    }
}