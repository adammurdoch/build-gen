package net.rubygrapefruit.gen.specs

interface BuildComponentVisitor {
    fun visitPlugin(plugin: PluginProductionSpec)

    fun visitApp(app: AppProductionSpec)

    fun visitLibrary(library: ExternalLibraryProductionSpec)

    fun visitInternalLibrary(library: InternalLibraryProductionSpec)
}