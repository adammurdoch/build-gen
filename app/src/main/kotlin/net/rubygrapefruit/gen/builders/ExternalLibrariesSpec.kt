package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryProductionSpec

/**
 * A mutable set of external libraries.
 */
interface ExternalLibrariesSpec {
    val libraries: List<ExternalLibraryProductionSpec>

    companion object {
        val empty = object : ExternalLibrariesSpec {
            override val libraries: List<ExternalLibraryProductionSpec>
                get() = emptyList()
        }
    }
}

class CompositeExternalLibrariesSpec : FinalizableBuilder<List<ExternalLibraryProductionSpec>>(), ExternalLibrariesSpec {
    private val contents = mutableListOf<ExternalLibrariesSpec>()

    override val libraries: List<ExternalLibraryProductionSpec>
        get() = value

    override fun calculateValue(): List<ExternalLibraryProductionSpec> {
        return contents.flatMap { it.libraries }
    }

    fun finalize() {
        finalizeOnRead()
    }

    fun add(library: ExternalLibrariesSpec) {
        assertCanMutate()
        contents.add(library)
    }
}
