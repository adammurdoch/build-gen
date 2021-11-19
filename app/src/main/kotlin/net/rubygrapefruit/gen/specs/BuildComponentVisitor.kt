package net.rubygrapefruit.gen.specs

interface BuildComponentVisitor {
    fun visitPlugin(pluginBundle: PluginBundleProductionSpec) {
    }

    fun visitApp(app: AppProductionSpec) {
    }

    fun visitLibrary(library: ExternalLibraryProductionSpec) {
    }

    fun visitInternalLibrary(library: InternalLibraryProductionSpec) {
    }

    fun visitEmptyComponent(component: EmptyComponentProductionSpec) {
    }
}