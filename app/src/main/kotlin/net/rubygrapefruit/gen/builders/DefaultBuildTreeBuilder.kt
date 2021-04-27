package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*
import java.nio.file.Path

/**
 * A mutable builder for the build tree structure.
 */
class DefaultBuildTreeBuilder(
    private val rootDir: Path,
    private val pluginSpecFactory: PluginSpecFactory,
    private val librarySpecFactory: LibrarySpecFactory
) : BuildTreeBuilder {
    private val builds = mutableListOf<BuildBuilderImpl>()
    private val mainBuild = BuildBuilderImpl("main build", "main", rootDir)

    init {
        builds.add(mainBuild)
    }

    override var includeConfigurationCacheProblems = false

    override fun <T> build(name: String, body: BuildBuilder.() -> T): T {
        require(!name.contains(':') && !name.contains('/'))
        val build = BuildBuilderImpl("build $name", name, rootDir.resolve(name))
        val result = body(build)
        mainBuild.childBuilds.add(build)
        builds.add(build)
        return result
    }

    override fun <T> mainBuild(body: BuildBuilder.() -> T): T {
        return body(mainBuild)
    }

    fun build(): BuildTreeSpec {
        return BuildTreeSpecImpl(rootDir, builds)
    }

    private class BuildTreeSpecImpl(
        override val rootDir: Path,
        override val builds: List<BuildSpec>
    ) : BuildTreeSpec

    private inner class BuildBuilderImpl(
        override val displayName: String,
        val baseName: String,
        override val rootDir: Path
    ) : BuildSpec, BuildBuilder {
        override val producesPlugins = mutableListOf<PluginProductionSpec>()
        override val usesPlugins = mutableListOf<PluginUseSpec>()
        override val usesLibraries = mutableListOf<ExternalLibraryUseSpec>()
        override var producesLibrary: ExternalLibraryProductionSpec? = null
        override val childBuilds = mutableListOf<BuildSpec>()

        override fun toString(): String {
            return displayName
        }

        override val includeConfigurationCacheProblems: Boolean
            get() = this@DefaultBuildTreeBuilder.includeConfigurationCacheProblems

        override fun producesPlugin(): PluginUseSpec {
            val plugin = pluginSpecFactory.plugin(baseName, "test.${baseName}")
            producesPlugins.add(plugin)
            return plugin.toUseSpec()
        }

        override fun producesLibrary(): ExternalLibraryUseSpec {
            if (producesLibrary == null) {
                val coordinates = ExternalLibraryCoordinates("test.$baseName", "core", "1.0")
                val libraryApi = librarySpecFactory.library(baseName)
                producesLibrary = ExternalLibraryProductionSpec(coordinates, libraryApi)
            }
            val library = producesLibrary!!
            return ExternalLibraryUseSpec(library.coordinates, library.spec.toApiSpec())
        }

        override fun <T> buildSrc(body: BuildBuilder.() -> T): T {
            val build = BuildBuilderImpl("buildSrc build", "buildSrc", rootDir.resolve("buildSrc"))
            val result = body(build)
            builds.add(build)
            return result
        }

        override fun requires(plugin: PluginUseSpec) {
            usesPlugins.add(plugin)
        }

        override fun requires(library: ExternalLibraryUseSpec) {
            usesLibraries.add(library)
        }

        override fun projects(body: RootProjectBuilder.() -> Unit): RootProjectSpec {
            val builder = DefaultRootProjectBuilder(this, librarySpecFactory)
            body(builder)
            return builder.build()
        }
    }
}