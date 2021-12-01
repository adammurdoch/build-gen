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
     * Add an application that uses the tooling API.
     */
    fun producesToolingApiClient()

    /**
     * Adds a plugin that this build should produce.
     */
    fun producesPlugin(): PluginRef

    /**
     * Adds a library that this build should produce. All applications and plugins of this build will by default depend on this library, either directly
     * or indirectly.
     */
    fun producesLibrary(): LibraryRef

    /**
     * Adds a library that this build should produce. Does not depend on any incoming libraries, so can be used to define cycles between builds. All applications, plugins and other libraries
     * will by default depend on this library, either directly or indirectly.
     */
    fun producesBottomLibrary(): LibraryRef

    /**
     * The projects of this build should use the given plugin.
     */
    fun requires(plugin: PluginRef)

    /**
     * The projects of this build should use the given library. All applications, libraries and plugins of this build will by default depend on the given
     * libraries, either directly or indirectly. Some internal components may not use the library.
     */
    fun requires(library: LibraryRef)

    /**
     * Include the given number of components, adding internal libraries to make up any shortfall.
     */
    fun includeComponents(componentCount: Int)

    /**
     * Defines the application project names to use for this build. Defaults to the build name.
     */
    fun appNames(vararg names: String)

    /**
     * Defines the exported library project names to use for this build. Defaults to the build name.
     */
    fun libraryNames(names: List<String>)

    /**
     * Defines the exported library project names to use for this build. Defaults to the build name.
     */
    fun libraryNames(vararg names: String) = libraryNames(names.toList())

    fun internalLibraryNames(topName: String, middleName: String, bottomName: String)
}