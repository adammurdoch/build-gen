package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryUseSpec
import net.rubygrapefruit.gen.specs.LibraryUseSpec
import net.rubygrapefruit.gen.specs.PluginUseSpec

/**
 * A mutable builder for the structure of a build in the tree.
 */
interface BuildBuilder {
    /**
     * The projects of this build should use the given plugin.
     */
    fun requires(plugin: PluginUseSpec)

    /**
     * The projects of this build should use the given library.
     */
    fun requires(library: ExternalLibraryUseSpec)
}