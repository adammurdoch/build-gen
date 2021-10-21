package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

interface ProjectBuilder {
    fun requiresPlugins(plugins: List<PluginUseSpec>)

    fun producesPlugin(plugin: PluginProductionSpec)

    fun producesApp(app: AppImplementationSpec)

    fun producesLibrary(library: LibraryProductionSpec): LibraryUseSpec

    fun producesLibrary(library: ExternalLibraryProductionSpec): LibraryUseSpec

    fun requiresExternalLibraries(libraries: List<ExternalLibraryUseSpec>)

    fun requiresLibraries(libraries: List<LibraryUseSpec>)

    fun requiresLibrary(library: LibraryUseSpec)
}