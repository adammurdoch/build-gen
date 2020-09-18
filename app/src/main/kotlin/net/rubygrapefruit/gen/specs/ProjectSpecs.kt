package net.rubygrapefruit.gen.specs

import java.nio.file.Path

sealed class ProjectSpec(
        val projectDir: Path,
        val usesPlugins: List<PluginUseSpec>,
        val producesPlugins: List<PluginProductionSpec>,
        val includeConfigurationCacheProblems: Boolean
)

class RootProjectSpec(
        projectDir: Path,
        usesPlugins: List<PluginUseSpec>,
        producesPlugins: List<PluginProductionSpec>,
        includeConfigurationCacheProblems: Boolean
) : ProjectSpec(projectDir, usesPlugins, producesPlugins, includeConfigurationCacheProblems)

class SubProjectSpec(
        val name: String,
        projectDir: Path,
        usesPlugins: List<PluginUseSpec>,
        producesPlugins: List<PluginProductionSpec>,
        includeConfigurationCacheProblems: Boolean
) : ProjectSpec(projectDir, usesPlugins, producesPlugins, includeConfigurationCacheProblems)