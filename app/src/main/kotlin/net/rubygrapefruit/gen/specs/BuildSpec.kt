package net.rubygrapefruit.gen.specs

import net.rubygrapefruit.gen.builders.RootProjectBuilder
import java.nio.file.Path

interface BuildSpec {
    val displayName: String
    val rootDir: Path
    val includeConfigurationCacheProblems: Boolean
    val childBuilds: List<BuildSpec>
    val usesPlugins: List<PluginUseSpec>
    val producesPlugins: List<PluginProductionSpec>
    val usesLibraries: List<LibraryUseSpec>
    val producesLibrary: LibraryProductionSpec?
    val projects: ProjectGraphSpec

    /**
     * Creates a project tree for this build.
     */
    fun projects(body: RootProjectBuilder.() -> Unit): RootProjectSpec
}