package net.rubygrapefruit.gen.specs

class AppProductionSpec(
    val baseName: BaseName,
    val implementationSpec: AppImplementationSpec,
    usesPlugins: List<PluginUseSpec>,
    usesLibraries: List<ExternalLibraryUseSpec>,
    usesImplementationLibraries: List<InternalLibraryProductionSpec>
) : BuildComponentProductionSpec(usesPlugins, usesLibraries, emptyList(), usesImplementationLibraries) {
    override fun accept(visitor: BuildComponentVisitor) {
        visitor.visitApp(this)
    }
}
