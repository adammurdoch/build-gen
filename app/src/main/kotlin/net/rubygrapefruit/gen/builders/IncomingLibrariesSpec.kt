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

class CompositeIncomingLibrariesSpec : FinalizableBuilder<List<ExternalLibraryUseSpec>>(), IncomingLibrariesSpec {
    private val contents = mutableListOf<IncomingLibrariesSpec>()

    override val libraries: List<ExternalLibraryUseSpec>
        get() = value

    override fun calculateValue(): List<ExternalLibraryUseSpec> {
        return contents.flatMap { it.libraries }
    }

    fun finalize() {
        finalizeOnRead()
    }

    fun add(library: IncomingLibrariesSpec) {
        assertCanMutate()
        contents.add(library)
    }
}
