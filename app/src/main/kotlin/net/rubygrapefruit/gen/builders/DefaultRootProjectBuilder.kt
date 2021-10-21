package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

/**
 * A builder for the root project of a build.
 */
class DefaultRootProjectBuilder(
    private val build: BuildSpec,
    rootProjectName: String
) : RootProjectBuilder {
    private val root = ProjectBuilderImpl(rootProjectName)
    private val children = LinkedHashMap<String, ProjectBuilderImpl>()

    override fun root(body: ProjectBuilder.() -> Unit) {
        body(root)
    }

    override fun <T> project(name: String, body: ProjectBuilder.() -> T): T {
        require(!name.contains(':') && !name.contains('/'))
        val project = children.getOrPut(name) { ProjectBuilderImpl(name) }
        return body(project)
    }

    fun build(): RootProjectSpec {
        val childSpecs = children.values.map {
            ChildProjectSpec(it.name, build.rootDir.resolve(it.name), it.usesPlugins, it.producesPlugins, it.producesApp, it.producesLibrary, it.usesLibraries, build.includeConfigurationCacheProblems)
        }
        return RootProjectSpec(root.name, build.rootDir, childSpecs, root.usesPlugins, root.producesPlugins, root.producesApp, root.producesLibrary, root.usesLibraries, build.includeConfigurationCacheProblems)
    }

    private inner class ProjectBuilderImpl(val name: String) : ProjectBuilder {
        val localCoordinates = LocalLibraryCoordinates(":$name")
        val usesPlugins = mutableListOf<PluginUseSpec>()
        val producesPlugins = mutableListOf<PluginProductionSpec>()
        val usesLibraries = mutableListOf<LibraryUseSpec>()
        var producesApp: AppImplementationSpec? = null
        var producesLibrary: LibraryImplementationSpec? = null

        override fun requiresPlugins(plugins: List<PluginUseSpec>) {
            usesPlugins.addAll(plugins)
        }

        override fun producesPlugin(plugin: PluginProductionSpec) {
            producesPlugins.add(plugin)
        }

        override fun producesApp(app: AppImplementationSpec) {
            require(producesLibrary == null && producesApp == null)
            producesApp = app
        }

        override fun producesLibrary(library: LibraryProductionSpec): LibraryUseSpec {
            require(producesLibrary == null && producesApp == null)
            producesLibrary = LibraryImplementationSpec(localCoordinates, null, library)
            return LibraryUseSpec(localCoordinates, library.toApiSpec())
        }

        override fun producesLibrary(library: ExternalLibraryProductionSpec): LibraryUseSpec {
            require(producesLibrary == null)
            producesLibrary = LibraryImplementationSpec(localCoordinates, library.coordinates, library.spec)
            return LibraryUseSpec(localCoordinates, library.spec.toApiSpec())
        }

        override fun requiresExternalLibraries(libraries: List<ExternalLibraryUseSpec>) {
            for (library in libraries) {
                usesLibraries.add(library.toUseSpec())
            }
        }

        override fun requiresLibraries(libraries: List<LibraryUseSpec>) {
            usesLibraries.addAll(libraries)
        }

        override fun requiresLibrary(library: LibraryUseSpec) {
            usesLibraries.add(library)
        }
    }
}