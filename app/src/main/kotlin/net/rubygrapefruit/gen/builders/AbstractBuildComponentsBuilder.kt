package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

abstract class AbstractBuildComponentsBuilder<T : BuildComponentProductionSpec> : AbstractComponentsBuilder<T>() {
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

    override final fun calculateContents(count: Int): List<T> {
        usesPlugins.finalize()
        usesExternalLibraries.finalize()
        usesInternalLibraries.finalize()
        usesIncomingLibraries.finalize()
        val result = mutableListOf<T>()
        for (i in 0 until count) {
            val component = createComponent(usesPlugins.plugins, usesExternalLibraries.libraries, usesInternalLibraries.libraries, usesIncomingLibraries.libraries)
            result.add(component)
        }
        return result
    }

    abstract fun createComponent(plugins: List<PluginUseSpec>, externalLibraries: List<ExternalLibraryProductionSpec>, internalLibraries: List<InternalLibraryProductionSpec>, incomingLibraries: List<ExternalLibraryUseSpec>): T

}