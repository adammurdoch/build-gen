package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryProductionSpec
import net.rubygrapefruit.gen.specs.ExternalLibraryUseSpec
import net.rubygrapefruit.gen.specs.LibraryUseSpec
import net.rubygrapefruit.gen.specs.PluginUseSpec

interface ProjectBuilder {
    fun requiresPlugins(plugins: List<PluginUseSpec>)

    fun requiresExternalLibraries(libraries: List<ExternalLibraryUseSpec>)

    fun producesExternalLibrary(producesLibrary: ExternalLibraryProductionSpec?)

    fun requiresLibrary(library: LibraryUseSpec)
}