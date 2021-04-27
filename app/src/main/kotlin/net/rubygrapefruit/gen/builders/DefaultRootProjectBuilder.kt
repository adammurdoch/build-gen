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

    override fun project(name: String, body: ProjectBuilder.() -> Unit): LibraryUseSpec? {
        require(!name.contains(':') && !name.contains('/'))
        val project = ProjectBuilderImpl(name)
        body(project)
        if (project.producesLibrary == null) {
            val spec = librarySpecFactory.maybeLibrary(name)
            if (spec != null) {
                project.producesLibrary = LocalLibraryProductionSpec(project.localCoordinates, null, spec)
            }
        }
        children.add(project)

        val library = project.producesLibrary
        return if (library != null) {
            LibraryUseSpec(library.localCoordinates, library.spec.toApiSpec())
        } else {
            null
        }
    }

    fun build(): RootProjectSpec {
        val childSpecs = children.map {
            ChildProjectSpec(it.name, build.rootDir.resolve(it.name), it.usesPlugins, emptyList(), it.producesLibrary, it.usesLibraries, build.includeConfigurationCacheProblems)
        }
        return RootProjectSpec(build.rootDir, childSpecs, root.usesPlugins, build.producesPlugins, root.producesLibrary, root.usesLibraries, build.includeConfigurationCacheProblems)
    }

    private inner class ProjectBuilderImpl(val name: String) : ProjectBuilder {
        val localCoordinates = LocalLibraryCoordinates(":$name")
        val usesPlugins = mutableListOf<PluginUseSpec>()
        val usesLibraries = mutableListOf<LibraryUseSpec>()
        var producesLibrary: LocalLibraryProductionSpec? = null

        override fun requiresPlugins(plugins: List<PluginUseSpec>) {
            usesPlugins.addAll(plugins)
        }

        override fun producesLibrary(library: ExternalLibraryProductionSpec?) {
            if (library == null) {
                this.producesLibrary = null
            } else {
                this.producesLibrary = LocalLibraryProductionSpec(localCoordinates, library.coordinates, library.spec)
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