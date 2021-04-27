package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

/**
 * A builder for the root project of a build.
 */
class DefaultRootProjectBuilder(
    private val build: BuildSpec,
    private val librarySpecFactory: LibrarySpecFactory
) : RootProjectBuilder {
    private val root = ProjectBuilderImpl("root")
    private val children = mutableListOf<ProjectBuilderImpl>()

    override fun root(body: ProjectBuilder.() -> Unit) {
        body(root)
    }

    override fun <T> project(name: String, body: ProjectBuilder.() -> T): T {
        require(!name.contains(':') && !name.contains('/'))
        val project = ProjectBuilderImpl(name)
        val result = body(project)
        children.add(project)
        return result
    }

    fun build(): RootProjectSpec {
        val childSpecs = children.map {
            ChildProjectSpec(it.name, build.rootDir.resolve(it.name), it.usesPlugins, it.producesPlugins, it.producesLibrary, it.usesLibraries, build.includeConfigurationCacheProblems)
        }
        return RootProjectSpec(build.rootDir, childSpecs, root.usesPlugins, root.producesPlugins, root.producesLibrary, root.usesLibraries, build.includeConfigurationCacheProblems)
    }

    private inner class ProjectBuilderImpl(val name: String) : ProjectBuilder {
        val localCoordinates = LocalLibraryCoordinates(":$name")
        val usesPlugins = mutableListOf<PluginUseSpec>()
        val producesPlugins = mutableListOf<PluginProductionSpec>()
        val usesLibraries = mutableListOf<LibraryUseSpec>()
        var producesLibrary: LocalLibraryProductionSpec? = null

        override fun requiresPlugins(plugins: List<PluginUseSpec>) {
            usesPlugins.addAll(plugins)
        }

        override fun producesPlugins(plugins: List<PluginProductionSpec>) {
            producesPlugins.addAll(plugins)
        }

        override fun producesLibrary(): LibraryUseSpec? {
            val spec = librarySpecFactory.maybeLibrary(name)
            if (spec == null) {
                producesLibrary = null
                return null
            } else {
                producesLibrary = LocalLibraryProductionSpec(localCoordinates, null, spec)
                return LibraryUseSpec(localCoordinates, spec.toApiSpec())
            }
        }

        override fun producesLibrary(library: ExternalLibraryProductionSpec?): LibraryUseSpec? {
            if (library == null) {
                return producesLibrary()
            } else {
                this.producesLibrary = LocalLibraryProductionSpec(localCoordinates, library.coordinates, library.spec)
                return LibraryUseSpec(localCoordinates, library.spec.toApiSpec())
            }
        }

        override fun requiresLibraries(libraries: List<ExternalLibraryUseSpec>) {
            for (library in libraries) {
                usesLibraries.add(LibraryUseSpec(library.coordinates, library.spec))
            }
        }

        override fun requiresLibrary(library: LibraryUseSpec?) {
            if (library != null) {
                usesLibraries.add(library)
            }
        }
    }
}