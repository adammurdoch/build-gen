package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

class PluginBuilder(
    private val projectNames: NameProvider,
    private val artifactType: String,
    private val pluginSpecFactory: PluginSpecFactory
) : SingleComponentBuilder<PluginBundleProductionSpec>() {
    val useSpec: PluginsSpec = object : PluginsSpec {
        override val plugins: List<PluginUseSpec>
            get() = contents.flatMap { it.useSpec }
    }

    override fun createComponent(
        plugins: List<PluginUseSpec>,
        externalLibraries: List<ExternalLibraryProductionSpec>,
        internalLibraries: List<InternalLibraryProductionSpec>,
        incomingLibraries: List<ExternalLibraryUseSpec>
    ): PluginBundleProductionSpec {
        val baseName = BaseName(projectNames.next())
        val spec = pluginSpecFactory.plugin(baseName, artifactType, "test.${baseName.lowerCaseDotSeparator}")
        return PluginBundleProductionSpec(baseName, listOf(spec), incomingLibraries, externalLibraries, internalLibraries)
    }
}