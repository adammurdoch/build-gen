package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.InternalLibraryProductionSpec

/**
 * A mutable set of internal libraries.
 */
interface InternalLibrariesSpec {
    val libraries: List<InternalLibraryProductionSpec>

    companion object {
        val empty = object : InternalLibrariesSpec {
            override val libraries: List<InternalLibraryProductionSpec>
                get() = emptyList()
        }
    }
}

class CompositeInternalLibrariesSpec : FinalizableBuilder<List<InternalLibraryProductionSpec>>(), InternalLibrariesSpec {
    private val contents = mutableListOf<InternalLibrariesSpec>()

    override val libraries: List<InternalLibraryProductionSpec>
        get() = value

    override fun calculateValue(): List<InternalLibraryProductionSpec> {
        return contents.flatMap { it.libraries }
    }

    fun finalize() {
        finalizeOnRead()
    }

    fun add(library: InternalLibrariesSpec) {
        assertCanMutate()
        contents.add(library)
    }
}
