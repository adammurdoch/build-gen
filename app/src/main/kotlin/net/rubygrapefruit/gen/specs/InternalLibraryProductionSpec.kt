package net.rubygrapefruit.gen.specs

class InternalLibraryProductionSpec(
    val baseName: BaseName,
    val spec: LibraryProductionSpec,
    usesPlugins: List<PluginUseSpec>,
    usesLibraries: List<ExternalLibraryUseSpec>,
    usesLibrariesFromSameBuild: List<ExternalLibraryProductionSpec>,
    usesImplementationLibraries: List<InternalLibraryProductionSpec>
) : BuildComponentProductionSpec(usesPlugins, usesLibraries, usesLibrariesFromSameBuild, usesImplementationLibraries) {
    override fun accept(visitor: BuildComponentVisitor) {
        visitor.visitInternalLibrary(this)
    }
}
