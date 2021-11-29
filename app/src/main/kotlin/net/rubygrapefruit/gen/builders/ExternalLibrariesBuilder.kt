package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryProductionSpec
import net.rubygrapefruit.gen.specs.NameProvider

class ExternalLibrariesBuilder(
    private val projectNames: NameProvider,
    private val group: String,
    private val librarySpecFactory: LibrarySpecFactory,
) : MultipleComponentsBuilder<ExternalLibraryProductionSpec, ExternalLibraryBuilder>() {
    val exportedLibraries: ExternalLibrariesSpec = object : ExternalLibrariesSpec {
        override val libraries: List<ExternalLibraryProductionSpec>
            get() = contents
    }

    override fun createBuilder(): ExternalLibraryBuilder {
        return ExternalLibraryBuilder(projectNames, group, librarySpecFactory)
    }
}