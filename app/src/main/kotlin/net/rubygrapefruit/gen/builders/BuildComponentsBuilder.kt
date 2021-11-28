package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

abstract class BuildComponentsBuilder<T : BuildComponentProductionSpec> : ComponentsBuilder<T>() {
    private val usesPlugins = CompositePluginsSpec()
    private val usesExternalLibraries = CompositeExternalLibrariesSpec()
    private val usesInternalLibraries = CompositeInternalLibrariesSpec()
    private val usesIncomingLibraries = CompositeIncomingLibrariesSpec()

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

    final override fun calculateContents(count: Int): List<T> {
        usesPlugins.finalize()
        usesExternalLibraries.finalize()
        usesInternalLibraries.finalize()
        usesIncomingLibraries.finalize()
        return createComponents(count, usesPlugins.plugins, usesExternalLibraries.libraries, usesInternalLibraries.libraries, usesIncomingLibraries.libraries)
    }

    protected abstract fun createComponents(
        count: Int,
        plugins: List<PluginUseSpec>,
        externalLibraries: List<ExternalLibraryProductionSpec>,
        internalLibraries: List<InternalLibraryProductionSpec>,
        incomingLibraries: List<ExternalLibraryUseSpec>
    ): List<T>
}