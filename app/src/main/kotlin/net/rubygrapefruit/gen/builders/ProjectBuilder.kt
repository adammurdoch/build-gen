package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

interface ProjectBuilder {
    fun producesPlugins(plugins: List<PluginProductionSpec>)

    fun requiresPlugins(plugins: List<PluginUseSpec>)

    fun producesLibrary(): LibraryUseSpec?

    /**
     * Produces a library. Uses the given spec if not null, otherwise uses a default spec.
     */
    fun producesLibrary(library: ExternalLibraryProductionSpec?): LibraryUseSpec?

    fun requiresExternalLibraries(libraries: List<ExternalLibraryUseSpec>)

    fun requiresLibraries(libraries: List<LibraryUseSpec>)

    fun requiresLibrary(library: LibraryUseSpec?)
}