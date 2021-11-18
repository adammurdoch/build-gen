package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

class ExternalLibrariesBuilder(
    private val projectNames: NameProvider,
    private val group: String,
    private val librarySpecFactory: LibrarySpecFactory,
) : AbstractComponentsBuilder<ExternalLibraryProductionSpec>() {
    private val usesPlugins = CompositePluginsSpec()
    private val usesExternalLibraries = CompositeExternalLibrariesSpec()
    private val usesInternalLibraries = CompositeInternalLibrariesSpec()
    private val usesIncomingLibraries = CompositeIncomingLibrariesSpec()

    val exportedLibraries: ExternalLibrariesSpec = object : ExternalLibrariesSpec {
        override val libraries: List<ExternalLibraryProductionSpec>
            get() {
                return contents
            }
    }

    val useSpec: IncomingLibrariesSpec = object : IncomingLibrariesSpec {
        override val libraries: List<ExternalLibraryUseSpec>
            get() = exportedLibraries.libraries.map { ExternalLibraryUseSpec(it.coordinates, it.spec.toApiSpec()) }
    }

    fun usesPlugins(spec: PluginsSpec) {
        assertNotFinalized()
        usesPlugins.add(spec)
    }

    fun usesLibraries(spec: ExternalLibrariesSpec) {
        assertNotFinalized()
        usesExternalLibraries.add(spec)
    }

    fun usesLibraries(spec: InternalLibrariesSpec) {
        assertNotFinalized()
        usesInternalLibraries.add(spec)
    }

    fun usesLibraries(spec: IncomingLibrariesSpec) {
        assertNotFinalized()
        usesIncomingLibraries.add(spec)
    }

    override fun calculateContents(count: Int): List<ExternalLibraryProductionSpec> {
        usesPlugins.finalize()
        usesExternalLibraries.finalize()
        usesInternalLibraries.finalize()
        usesIncomingLibraries.finalize()
        val result = mutableListOf<ExternalLibraryProductionSpec>()
        for (i in 0 until count) {
            val name = projectNames.next()
            val coordinates = ExternalLibraryCoordinates(group, name, "1.0")
            val libraryApi = librarySpecFactory.library(BaseName(name))
            val library = ExternalLibraryProductionSpec(
                coordinates,
                libraryApi,
                usesPlugins.plugins,
                usesIncomingLibraries.libraries,
                usesExternalLibraries.libraries,
                usesInternalLibraries.libraries
            )
            result.add(library)
        }
        return result
    }
}