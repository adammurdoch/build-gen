package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryUseSpec

/**
 * A mutable set of external libraries from another build.
 */
interface IncomingLibrariesSpec {
    val libraries: List<ExternalLibraryUseSpec>

    companion object {
        val empty = object : IncomingLibrariesSpec {
            override val libraries: List<ExternalLibraryUseSpec>
                get() = emptyList()
        }
    }
}

class CompositeIncomingLibrariesSpec : IncomingLibrariesSpec {
    private val contents = mutableListOf<IncomingLibrariesSpec>()
    private var finalized = false

    override val libraries: List<ExternalLibraryUseSpec>
        get() {
            require(finalized)
            return contents.flatMap { it.libraries }
        }

    fun finalize() {
        finalized = true
    }

    fun add(library: IncomingLibrariesSpec) {
        require(!finalized)
        contents.add(library)
    }
}
