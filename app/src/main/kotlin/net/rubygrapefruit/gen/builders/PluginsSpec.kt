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

class DefaultPluginsSpec : PluginsSpec {
    private val contents = mutableListOf<PluginUseSpec>()
    private var finalized = false

    override val plugins: List<PluginUseSpec>
        get() {
            require(finalized)
            return contents
        }

    fun finalize() {
        finalized = true
    }

    fun add(plugin: PluginUseSpec) {
        require(!finalized)
        contents.add(plugin)
    }
}