package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryProductionSpec
import net.rubygrapefruit.gen.specs.NameProvider

class ExternalLibrariesBuilder(
    private val projectNames: NameProvider,
    private val group: String,
    private val librarySpecFactory: LibrarySpecFactory,
) : ComponentsBuilder<ExternalLibraryProductionSpec>() {
    private val builders = mutableListOf<SingleExternalLibraryBuilder>()

    override val currentSize: Int
        get() = builders.fold(0) { i, v -> i + v.currentSize }

    val exportedLibraries: ExternalLibrariesSpec = object : ExternalLibrariesSpec {
        override val libraries: List<ExternalLibraryProductionSpec>
            get() = contents
    }

    fun add(): SingleExternalLibraryBuilder {
        assertNotFinalized()
        val library = SingleExternalLibraryBuilder(projectNames, group, librarySpecFactory)
        builders.add(library)
        return library
    }

    override fun calculateContents(): List<ExternalLibraryProductionSpec> {
        return builders.flatMap { it.contents }
    }
}