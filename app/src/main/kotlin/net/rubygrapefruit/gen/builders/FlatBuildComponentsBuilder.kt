package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

/**
 * Creates zero or more components in a flat structure.
 */
abstract class FlatBuildComponentsBuilder<T : BuildComponentProductionSpec> : BuildComponentsBuilder<T>() {
    private var count = 0

    override val currentSize: Int
        get() = count

    fun add() {
        assertNotFinalized()
        count++
    }

    override fun createComponents(
        plugins: List<PluginUseSpec>,
        externalLibraries: List<ExternalLibraryProductionSpec>,
        internalLibraries: List<InternalLibraryProductionSpec>,
        incomingLibraries: List<ExternalLibraryUseSpec>
    ): List<T> {
        val result = mutableListOf<T>()
        for (i in 0 until count) {
            val component = createComponent(plugins, externalLibraries, internalLibraries, incomingLibraries)
            result.add(component)
        }
        return result
    }

    abstract fun createComponent(plugins: List<PluginUseSpec>, externalLibraries: List<ExternalLibraryProductionSpec>, internalLibraries: List<InternalLibraryProductionSpec>, incomingLibraries: List<ExternalLibraryUseSpec>): T
}