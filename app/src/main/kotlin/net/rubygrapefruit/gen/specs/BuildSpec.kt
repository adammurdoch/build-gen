package net.rubygrapefruit.gen.specs

import net.rubygrapefruit.gen.builders.DefaultRootProjectBuilder
import net.rubygrapefruit.gen.builders.LibrarySpecFactory
import net.rubygrapefruit.gen.builders.RootProjectBuilder
import java.nio.file.Path

class BuildSpec(
    val displayName: String,
    val rootDir: Path,
    val includeConfigurationCacheProblems: Boolean,
    val childBuilds: List<BuildSpec>,
    val usesPlugins: List<PluginUseSpec>,
    val producesPlugins: List<PluginProductionSpec>,
    val producesLibraries: List<ExternalLibraryProductionSpec>,
    val producesApps: List<AppProductionSpec>,
    val projectNames: NameProvider,
    private val librarySpecFactory: LibrarySpecFactory
) {
    /**
     * Creates a project tree for this build.
     */
    fun projects(body: RootProjectBuilder.() -> Unit): RootProjectSpec {
        val builder = DefaultRootProjectBuilder(this, librarySpecFactory)
        body(builder)
        return builder.build()
    }
}