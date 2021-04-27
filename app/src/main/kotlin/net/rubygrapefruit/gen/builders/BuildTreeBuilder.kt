package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.ExternalLibraryUseSpec
import net.rubygrapefruit.gen.specs.PluginUseSpec

/**
 * A mutable builder for the build tree structure.
 */
interface BuildTreeBuilder {
    var includeConfigurationCacheProblems: Boolean

    fun addBuildSrc()

    /**
     * Adds a child build that produces a plugin, returning the spec for using the plugin.
     */
    fun addBuildLogicBuild(): PluginUseSpec

    /**
     * Adds a child build that produces a library, returning the spec for using the library.
     */
    fun addProductionBuild(name: String, body: BuildBuilder.() -> Unit): ExternalLibraryUseSpec

    fun mainBuild(body: BuildBuilder.() -> Unit)
}