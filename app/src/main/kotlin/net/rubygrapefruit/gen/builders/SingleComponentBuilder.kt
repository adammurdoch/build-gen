package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

abstract class SingleComponentBuilder<T : BuildComponentProductionSpec>(
) : BuildComponentsBuilder<T>() {
    override val currentSize: Int
        get() = 1

    override fun createComponents(plugins: List<PluginUseSpec>, externalLibraries: List<ExternalLibraryProductionSpec>, internalLibraries: List<InternalLibraryProductionSpec>, incomingLibraries: List<ExternalLibraryUseSpec>): List<T> {
        return listOf(createComponent(plugins, externalLibraries, internalLibraries, incomingLibraries))
    }

    protected abstract fun createComponent(
        plugins: List<PluginUseSpec>,
        externalLibraries: List<ExternalLibraryProductionSpec>,
        internalLibraries: List<InternalLibraryProductionSpec>,
        incomingLibraries: List<ExternalLibraryUseSpec>
    ): T
}