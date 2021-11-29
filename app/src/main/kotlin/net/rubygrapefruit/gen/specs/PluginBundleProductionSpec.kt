package net.rubygrapefruit.gen.specs

class PluginBundleProductionSpec(
    val baseName: BaseName,
    val plugins: List<PluginProductionSpec>,
    usesLibraries: List<ExternalLibraryUseSpec>,
    usesLibrariesFromSameBuild: List<ExternalLibraryProductionSpec>,
    usesImplementationLibraries: List<InternalLibraryProductionSpec>
) : BuildComponentProductionSpec(emptyList(), usesLibraries, usesLibrariesFromSameBuild, usesImplementationLibraries) {
    val useSpec: List<PluginUseSpec>
        get() = plugins.map { it.toUseSpec() }

    override fun accept(visitor: BuildComponentVisitor) {
        visitor.visitPlugin(this)
    }
}