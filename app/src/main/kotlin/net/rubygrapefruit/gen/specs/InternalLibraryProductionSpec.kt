package net.rubygrapefruit.gen.specs

class InternalLibraryProductionSpec(
    val baseName: BaseName,
    val spec: LibraryProductionSpec,
    usesPlugins: List<PluginUseSpec>
) : BuildComponentProductionSpec(usesPlugins, emptyList(), emptyList(), emptyList()) {
    override fun accept(visitor: BuildComponentVisitor) {
        visitor.visitInternalLibrary(this)
    }
}
