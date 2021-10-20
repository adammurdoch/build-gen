package net.rubygrapefruit.gen.builders

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
    fun pluginBuild(name: String): BuildBuilder

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
     * Includes this build in itself
     */
    fun includeSelf()

    /**
     * Adds an application.
     */
    fun producesApp()

    /**
     * Adds a plugin that this build should produce.
     */
    fun producesPlugin(): PluginRef

    /**
     * Adds a library that this build should produce.
     */
    fun producesLibrary(): LibraryRef

    /**
     * Adds multiple libraries that this build should produce.
     */
    fun producesLibraries(): LibrariesRef

    /**
     * The projects of this build should use the given plugin.
     */
    fun requires(plugin: PluginRef)

    /**
     * The projects of this build should use the given library.
     */
    fun requires(library: LibraryRef)

    /**
     * Include the given number of components, adding internal libraries to make up any shortfall.
     */
    fun includeComponents(componentCount: Int)

    /**
     * Defines some project names to use for this build
     */
    fun projectNames(names: List<String>)
}