package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryProductionSpec
import net.rubygrapefruit.gen.specs.LibraryUseSpec
import net.rubygrapefruit.gen.specs.PluginUseSpec

interface ProjectBuilder {
    fun requiresPlugins(plugins: List<PluginUseSpec>)

    fun producesExternalLibrary(producesLibrary: ExternalLibraryProductionSpec?)

    fun requiresLibraries(libraries: List<LibraryUseSpec>)

    fun requiresLibrary(library: LibraryUseSpec)
}