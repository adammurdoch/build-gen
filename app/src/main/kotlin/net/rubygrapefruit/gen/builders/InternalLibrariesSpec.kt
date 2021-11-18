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