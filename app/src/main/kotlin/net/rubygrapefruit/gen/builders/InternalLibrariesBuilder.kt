package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

/**
 * Builds a set of zero or more internal libraries.
 */
class InternalLibrariesBuilder(
    private val projectNames: NameProvider,
    private val librarySpecFactory: LibrarySpecFactory,
) : FlatBuildComponentsBuilder<InternalLibraryProductionSpec>() {
    val exportedLibraries: InternalLibrariesSpec = object : InternalLibrariesSpec {
        override val libraries: List<InternalLibraryProductionSpec>
            get() {
                return contents
            }
    }

    override fun createComponent(
        plugins: List<PluginUseSpec>,
        externalLibraries: List<ExternalLibraryProductionSpec>,
        internalLibraries: List<InternalLibraryProductionSpec>,
        incomingLibraries: List<ExternalLibraryUseSpec>
    ): InternalLibraryProductionSpec {
        val libraryName = BaseName(projectNames.next())
        val spec = librarySpecFactory.library(libraryName)
        return InternalLibraryProductionSpec(libraryName, spec, plugins, incomingLibraries, externalLibraries, internalLibraries)
    }
}