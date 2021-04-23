package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

/**
 * A builder for the root project of a build.
 */
class DefaultRootProjectBuilder(private val build: BuildSpec) : RootProjectBuilder {
    private val root = ProjectBuilderImpl("root")
    private val children = mutableListOf<ProjectBuilderImpl>()

    override fun root(body: ProjectBuilder.() -> Unit) {
        body(root)
    }

    override fun project(name: String, body: ProjectBuilder.() -> Unit): LibraryUseSpec {
        require(!name.contains(':') && !name.contains('/'))
        val project = ProjectBuilderImpl(name)
        body(project)
        children.add(project)
        return LibraryUseSpec(":$name")
    }

    fun build(): RootProjectSpec {
        val childSpecs = children.map {
            ChildProjectSpec(it.name, build.rootDir.resolve(it.name), it.usesPlugins, emptyList(), it.usesExternalLibraries, it.producesLibrary, it.usesLibraries, build.includeConfigurationCacheProblems)
        }
        return RootProjectSpec(build.rootDir, childSpecs, root.usesPlugins, build.producesPlugins, root.usesExternalLibraries, root.producesLibrary, root.usesLibraries, build.includeConfigurationCacheProblems)
    }

    private inner class ProjectBuilderImpl(val name: String) : ProjectBuilder {
        val usesPlugins = mutableListOf<PluginUseSpec>()
        val usesExternalLibraries = mutableListOf<ExternalLibraryUseSpec>()
        val usesLibraries = mutableListOf<LibraryUseSpec>()
        var producesLibrary: ExternalLibraryProductionSpec? = null

        override fun requiresPlugins(plugins: List<PluginUseSpec>) {
            usesPlugins.addAll(plugins)
        }

        override fun requiresExternalLibraries(libraries: List<ExternalLibraryUseSpec>) {
            usesExternalLibraries.addAll(libraries)
        }

        override fun producesExternalLibrary(producesLibrary: ExternalLibraryProductionSpec?) {
            this.producesLibrary = producesLibrary
        }

        override fun requiresLibrary(library: LibraryUseSpec) {
            usesLibraries.add(library)
        }
    }
}