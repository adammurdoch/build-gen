package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.LibraryProductionSpec
import net.rubygrapefruit.gen.specs.LibraryUseSpec
import net.rubygrapefruit.gen.specs.PluginUseSpec

interface ProjectBuilder {
    fun requiresPlugins(plugins: List<PluginUseSpec>)

    fun producesLibrary(library: LibraryProductionSpec?)

    fun requiresLibraries(libraries: List<LibraryUseSpec>)

    fun requiresLibrary(library: LibraryUseSpec?)
}