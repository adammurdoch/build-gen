package net.rubygrapefruit.gen.specs

import java.nio.file.Path

sealed class ProjectSpec(
    val projectDir: Path,
    val usesPlugins: List<PluginUseSpec>,
    val producesPlugins: List<PluginProductionSpec>,
    val producesLibrary: LocalLibraryProductionSpec?,
    val usesLibraries: List<LibraryUseSpec>,
    val includeConfigurationCacheProblems: Boolean
)

class RootProjectSpec(
    projectDir: Path,
    val children: List<ChildProjectSpec>,
    usesPlugins: List<PluginUseSpec>,
    producesPlugins: List<PluginProductionSpec>,
    producesLibrary: LocalLibraryProductionSpec?,
    usesLibraries: List<LibraryUseSpec>,
    includeConfigurationCacheProblems: Boolean
) : ProjectSpec(projectDir, usesPlugins, producesPlugins, producesLibrary, usesLibraries, includeConfigurationCacheProblems) {
    /**
     * Includes this project and its children
     */
    val projects: List<ProjectSpec> = listOf(this) + children
}

class ChildProjectSpec(
    val name: String,
    projectDir: Path,
    usesPlugins: List<PluginUseSpec>,
    producesPlugins: List<PluginProductionSpec>,
    producesLibrary: LocalLibraryProductionSpec?,
    usesLibraries: List<LibraryUseSpec>,
    includeConfigurationCacheProblems: Boolean
) : ProjectSpec(projectDir, usesPlugins, producesPlugins, producesLibrary, usesLibraries, includeConfigurationCacheProblems)
