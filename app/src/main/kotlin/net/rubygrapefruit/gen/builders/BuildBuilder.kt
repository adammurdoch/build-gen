package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryUseSpec
import net.rubygrapefruit.gen.specs.PluginUseSpec

/**
 * A mutable builder for the structure of a build in the tree.
 */
interface BuildBuilder {
    /**
     * The build should have a buildSrc build
     */
    fun <T> buildSrc(body: BuildBuilder.() -> T): T

    /**
     * The build should produce a plugin.
     */
    fun producesPlugin(): PluginUseSpec

    /**
     * The build should produce a library.
     */
    fun producesLibrary(): ExternalLibraryUseSpec

    /**
     * The projects of this build should use the given plugin.
     */
    fun requires(plugin: PluginUseSpec)

    /**
     * The projects of this build should use the given library.
     */
    fun requires(library: ExternalLibraryUseSpec)
}