package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

class PluginsBuilder(
    private val projectNames: NameProvider,
    private val artifactType: String,
    private val pluginSpecFactory: PluginSpecFactory
) : AbstractBuildComponentsBuilder<PluginBundleProductionSpec>() {
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
        require(externalLibraries.isEmpty())
        require(internalLibraries.isEmpty())
        require(incomingLibraries.isEmpty())
        val baseName = BaseName(projectNames.next())
        val spec = pluginSpecFactory.plugin(baseName, artifactType, "test.${baseName.lowerCaseDotSeparator}")
        return PluginBundleProductionSpec(baseName, listOf(spec))
    }
}