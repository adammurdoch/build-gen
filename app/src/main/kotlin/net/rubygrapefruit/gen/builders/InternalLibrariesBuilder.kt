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
) : AbstractComponentsBuilder<InternalLibraryProductionSpec>() {
    private val plugins = CompositePluginsSpec()

    val exportedLibraries: InternalLibrariesSpec = object : InternalLibrariesSpec {
        override val libraries: List<InternalLibraryProductionSpec>
            get() {
                return contents
            }
    }

    fun usesPlugins(spec: PluginsSpec) {
        assertNotFinalized()
        plugins.add(spec)
    }

    override fun calculateContents(count: Int): List<InternalLibraryProductionSpec> {
        plugins.finalize()
        val allPlugins = plugins.plugins
        val result = mutableListOf<InternalLibraryProductionSpec>()
        for (i in 0 until count) {
            val libraryName = BaseName(projectNames.next())
            val spec = librarySpecFactory.library(libraryName)
            result.add(InternalLibraryProductionSpec(libraryName, spec, allPlugins))
        }
        return result
    }
}