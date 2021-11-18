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

class CompositeInternalLibrariesSpec : InternalLibrariesSpec {
    private val contents = mutableListOf<InternalLibrariesSpec>()
    private var finalized = false

    override val libraries: List<InternalLibraryProductionSpec>
        get() {
            require(finalized)
            return contents.flatMap { it.libraries }
        }

    fun finalize() {
        finalized = true
    }

    fun add(library: InternalLibrariesSpec) {
        require(!finalized)
        contents.add(library)
    }
}
