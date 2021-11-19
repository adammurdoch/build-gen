package net.rubygrapefruit.gen.specs

class PluginBundleProductionSpec(
    val baseName: BaseName,
    val plugins: List<PluginProductionSpec>
) : BuildComponentProductionSpec(emptyList(), emptyList(), emptyList(), emptyList()) {
    val useSpec: List<PluginUseSpec>
        get() = plugins.map { it.toUseSpec() }

    override fun accept(visitor: BuildComponentVisitor) {
        visitor.visitPlugin(this)
    }
}