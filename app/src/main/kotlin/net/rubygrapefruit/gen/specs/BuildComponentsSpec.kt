package net.rubygrapefruit.gen.specs

import java.util.function.Consumer

sealed class BuildComponentsSpec {
    /**
     * Visits the components of this spec, in any order.
     */
    abstract fun visit(visitor: Consumer<BuildComponentProductionSpec>)
}

class FixedComponentsSpec(
    private val producesPlugins: List<PluginProductionSpec>,
    private val producesLibraries: List<ExternalLibraryProductionSpec>,
    private val producesApps: List<AppProductionSpec>,
    private val producesInternalLibraries: List<InternalLibraryProductionSpec>
) : BuildComponentsSpec() {
    override fun visit(visitor: Consumer<BuildComponentProductionSpec>) {
        producesInternalLibraries.visit(visitor)
        producesLibraries.visit(visitor)
        producesApps.visit(visitor)
        producesPlugins.visit(visitor)
    }

    private fun List<BuildComponentProductionSpec>.visit(visitor: Consumer<BuildComponentProductionSpec>) {
        for (spec in this) {
            visitor.accept(spec)
        }
    }
}