package net.rubygrapefruit.gen.specs

class InternalLibrarySpec(
    val baseName: BaseName,
    val spec: LibraryProductionSpec,
    usesPlugins: List<PluginUseSpec>
) : BuildComponentProductionSpec(usesPlugins, emptyList(), emptyList(), emptyList())
