package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

interface ProjectBuilder {
    fun producesPlugins(plugins: List<PluginProductionSpec>)

    fun requiresPlugins(plugins: List<PluginUseSpec>)

    fun producesLibrary(library: ExternalLibraryProductionSpec?)

    fun requiresLibraries(libraries: List<ExternalLibraryUseSpec>)

    fun requiresLibrary(library: LibraryUseSpec?)
}