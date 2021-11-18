package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.BaseName
import net.rubygrapefruit.gen.specs.InternalLibraryProductionSpec
import net.rubygrapefruit.gen.specs.NameProvider

/**
 * Builds a set of zero or more internal libraries.
 */
class InternalLibrariesBuilder(
    private var projectNames: NameProvider,
    private val librarySpecFactory: LibrarySpecFactory,
) {
    private var count = 0
    private val plugins = mutableListOf<PluginsSpec>()
    private var libraries: List<InternalLibraryProductionSpec>? = null

    val currentSize: Int
        get() = count

    val exportedInternalLibraries: InternalLibrariesSpec = object : InternalLibrariesSpec {
        override val libraries: List<InternalLibraryProductionSpec>
            get() {
                assertFinalized()
                return contents
            }
    }

    val contents: List<InternalLibraryProductionSpec>
        get() {
            assertFinalized()
            return libraries!!
        }

    fun usesPlugins(spec: PluginsSpec) {
        assertNotFinalized()
        plugins.add(spec)
    }

    fun add() {
        assertNotFinalized()
        count++
    }

    private fun assertFinalized() {
        require(libraries != null)
    }

    private fun assertNotFinalized() {
        require(libraries == null)
    }

    fun finalize() {
        if (libraries == null) {
            val allPlugins = plugins.flatMap { it.plugins }.distinct()
            val result = mutableListOf<InternalLibraryProductionSpec>()
            for (i in 0 until count) {
                val libraryName = BaseName(projectNames.next())
                val spec = librarySpecFactory.library(libraryName)
                result.add(InternalLibraryProductionSpec(libraryName, spec, allPlugins))
            }
            libraries = result
        }
    }
}