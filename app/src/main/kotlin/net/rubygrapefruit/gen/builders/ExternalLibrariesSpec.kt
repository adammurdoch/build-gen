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

class CompositeExternalLibrariesSpec : ExternalLibrariesSpec {
    private val contents = mutableListOf<ExternalLibrariesSpec>()
    private var finalized = false

    override val libraries: List<ExternalLibraryProductionSpec>
        get() {
            require(finalized)
            return contents.flatMap { it.libraries }
        }

    fun finalize() {
        finalized = true
    }

    fun add(library: ExternalLibrariesSpec) {
        require(!finalized)
        contents.add(library)
    }
}
