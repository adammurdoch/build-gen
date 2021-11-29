package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.BuildComponentProductionSpec

abstract class ZeroOrMoreBuildComponentsBuilder<T : BuildComponentProductionSpec> : BuildComponentsBuilder<T>() {
    private var count = 0

    override val currentSize: Int
        get() = count

    fun add(extra: Int) {
        assertNotFinalized()
        count += extra
    }

    override fun createComponents(
        plugins: PluginsSpec,
        externalLibraries: ExternalLibrariesSpec,
        internalLibraries: InternalLibrariesSpec,
        incomingLibraries: IncomingLibrariesSpec
    ): List<T> {
        return createComponents(count, plugins, externalLibraries, internalLibraries, incomingLibraries)
    }

    protected abstract fun createComponents(
        count: Int,
        plugins: PluginsSpec,
        externalLibraries: ExternalLibrariesSpec,
        internalLibraries: InternalLibrariesSpec,
        incomingLibraries: IncomingLibrariesSpec
    ): List<T>
}