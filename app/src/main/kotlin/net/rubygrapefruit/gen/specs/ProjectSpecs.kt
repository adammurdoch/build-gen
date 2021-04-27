package net.rubygrapefruit.gen.specs

import java.nio.file.Path

sealed class ProjectSpec(
    val projectDir: Path,
    val usesPlugins: List<PluginUseSpec>,
    val producesPlugins: List<PluginProductionSpec>,
    val producesExternalLibrary: ExternalLibraryProductionSpec?,
    val usesLibraries: List<LibraryUseSpec>,
    val includeConfigurationCacheProblems: Boolean
)

class RootProjectSpec(
    projectDir: Path,
    val children: List<ChildProjectSpec>,
    usesPlugins: List<PluginUseSpec>,
    producesPlugins: List<PluginProductionSpec>,
    producesExternalLibrary: ExternalLibraryProductionSpec?,
    usesLibraries: List<LibraryUseSpec>,
    includeConfigurationCacheProblems: Boolean
) : ProjectSpec(projectDir, usesPlugins, producesPlugins, producesExternalLibrary, usesLibraries, includeConfigurationCacheProblems) {
    val projects: List<ProjectSpec> = listOf(this) + children
}

class ChildProjectSpec(
    val name: String,
    projectDir: Path,
    usesPlugins: List<PluginUseSpec>,
    producesPlugins: List<PluginProductionSpec>,
    producesExternalLibrary: ExternalLibraryProductionSpec?,
    usesLibraries: List<LibraryUseSpec>,
    includeConfigurationCacheProblems: Boolean
) : ProjectSpec(projectDir, usesPlugins, producesPlugins, producesExternalLibrary, usesLibraries, includeConfigurationCacheProblems)
