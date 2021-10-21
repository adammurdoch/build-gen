package net.rubygrapefruit.gen.specs

import java.nio.file.Path

sealed class ProjectSpec(
    val name: String,
    val projectDir: Path,
    val usesPlugins: List<PluginUseSpec>,
    val producesPlugins: List<PluginProductionSpec>,
    val producesApp: AppImplementationSpec?,
    val producesLibrary: LibraryImplementationSpec?,
    val usesLibraries: List<LibraryUseSpec>,
    val includeConfigurationCacheProblems: Boolean
)

class RootProjectSpec(
    name: String,
    projectDir: Path,
    val children: List<ChildProjectSpec>,
    usesPlugins: List<PluginUseSpec>,
    producesPlugins: List<PluginProductionSpec>,
    producesApp: AppImplementationSpec?,
    producesLibrary: LibraryImplementationSpec?,
    usesLibraries: List<LibraryUseSpec>,
    includeConfigurationCacheProblems: Boolean
) : ProjectSpec(name, projectDir, usesPlugins, producesPlugins, producesApp, producesLibrary, usesLibraries, includeConfigurationCacheProblems) {
    /**
     * Includes this project and its children
     */
    val projects: List<ProjectSpec> = listOf(this) + children
}

class ChildProjectSpec(
    name: String,
    projectDir: Path,
    usesPlugins: List<PluginUseSpec>,
    producesPlugins: List<PluginProductionSpec>,
    producesApp: AppImplementationSpec?,
    producesLibrary: LibraryImplementationSpec?,
    usesLibraries: List<LibraryUseSpec>,
    includeConfigurationCacheProblems: Boolean
) : ProjectSpec(name, projectDir, usesPlugins, producesPlugins, producesApp, producesLibrary, usesLibraries, includeConfigurationCacheProblems)
