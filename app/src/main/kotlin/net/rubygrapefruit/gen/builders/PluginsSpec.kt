package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.PluginUseSpec

/**
 * A mutable set of plugin specs.
 */
interface PluginsSpec {
    val plugins: List<PluginUseSpec>

    companion object {
        val empty = object : PluginsSpec {
            override val plugins: List<PluginUseSpec>
                get() = emptyList()
        }
    }
}

class CompositePluginsSpec : FinalizableBuilder<List<PluginUseSpec>>(), PluginsSpec {
    private val contents = mutableListOf<PluginsSpec>()

    override val plugins: List<PluginUseSpec>
        get() = value

    override fun calculateValue(): List<PluginUseSpec> {
        return contents.flatMap { it.plugins }.distinct()
    }

    fun finalize() {
        finalizeOnRead()
    }

    fun add(plugin: PluginsSpec) {
        assertCanMutate()
        contents.add(plugin)
    }
}