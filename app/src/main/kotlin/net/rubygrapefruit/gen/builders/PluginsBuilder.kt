package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.NameProvider
import net.rubygrapefruit.gen.specs.PluginBundleProductionSpec
import net.rubygrapefruit.gen.specs.PluginUseSpec

class PluginsBuilder(
    private val projectNames: NameProvider,
    private val artifactType: String,
    private val pluginSpecFactory: PluginSpecFactory
) : CompositeComponentsBuilder<PluginBundleProductionSpec, PluginBuilder>() {
    val useSpec: PluginsSpec = object : PluginsSpec {
        override val plugins: List<PluginUseSpec>
            get() = contents.flatMap { it.useSpec }
    }

    override fun createBuilder(): PluginBuilder {
        return PluginBuilder(projectNames, artifactType, pluginSpecFactory)
    }
}