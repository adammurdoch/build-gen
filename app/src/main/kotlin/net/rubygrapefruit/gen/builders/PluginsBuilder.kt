package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

class PluginsBuilder(
    private val projectNames: NameProvider,
    private val artifactType: String,
    private val pluginSpecFactory: PluginSpecFactory
) : AbstractBuildComponentsBuilder<PluginProductionSpec>() {
    val useSpec: PluginsSpec = object : PluginsSpec {
        override val plugins: List<PluginUseSpec>
            get() = contents.map { it.toUseSpec() }
    }

    override fun createComponent(
        plugins: List<PluginUseSpec>,
        externalLibraries: List<ExternalLibraryProductionSpec>,
        internalLibraries: List<InternalLibraryProductionSpec>,
        incomingLibraries: List<ExternalLibraryUseSpec>
    ): PluginProductionSpec {
        require(externalLibraries.isEmpty())
        require(internalLibraries.isEmpty())
        require(incomingLibraries.isEmpty())
        val baseName = BaseName(projectNames.next())
        return pluginSpecFactory.plugin(baseName, artifactType, "test.${baseName.lowerCaseDotSeparator}")
    }
}