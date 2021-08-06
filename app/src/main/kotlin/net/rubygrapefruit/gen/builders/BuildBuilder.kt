package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryUseSpec
import net.rubygrapefruit.gen.specs.PluginUseSpec

/**
 * A mutable builder for the structure of a build in the tree.
 */
interface BuildBuilder {
    /**
     * Adds a buildSrc build
     */
    fun <T> buildSrc(body: BuildBuilder.() -> T): T

    /**
     * Adds a child build that produces plugins.
     */
    fun <T> pluginBuild(name: String, body: BuildBuilder.() -> T): T

    /**
     * Adds a child build.
     */
    fun build(name: String): BuildBuilder

    /**
     * Adds a child build.
     */
    fun <T> build(name: String, body: BuildBuilder.() -> T): T

    /**
     * Adds a plugin that this build should produce.
     */
    fun producesPlugin(): PluginUseSpec

    /**
     * Adds a library that this build should produce.
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

    /**
     * Defines some project names to use for this build
     */
    fun projectNames(names: List<String>)
}