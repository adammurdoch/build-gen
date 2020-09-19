package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.*
import java.nio.file.Files

class BuildContentsGenerator(
        private val scriptGenerator: ScriptGenerator,
        private val assemblers: List<Assembler<BuildContentsBuilder>>,
        private val projectGenerator: Generator<ProjectSpec>
) {
    fun buildContents(): Generator<BuildSpec> = Generator.of { generationContext ->
        Files.createDirectories(rootDir)

        val rootProject = projects(this)

        val settings = scriptGenerator.settings(rootDir)
        settings.apply {
            for (project in rootProject.children) {
                includeProject(project.name)
            }
            for (childBuild in childBuilds) {
                includeBuild(rootDir.relativize(childBuild.rootDir).toString())
            }
        }

        val builder = BuildContentsBuilder(this, settings)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }

        settings.complete()

        generationContext.apply(rootProject.projects, projectGenerator)
    }

    private fun projects(build: BuildSpec): RootProjectSpec {
        return when (build.projects) {
            ProjectGraphSpec.RootProject -> RootProjectSpec(build.rootDir, emptyList(), build.usesPlugins, build.producesPlugins, emptyList(), build.includeConfigurationCacheProblems)
            ProjectGraphSpec.MultipleProjects -> {
                val libs = if (build.usesPlugins.isEmpty()) emptyList() else listOf(LibraryUseSpec(":lib"))
                val children = listOf(
                        ChildProjectSpec("lib", build.rootDir.resolve("lib"), build.usesPlugins, emptyList(), emptyList(), build.includeConfigurationCacheProblems),
                        ChildProjectSpec("app", build.rootDir.resolve("app"), build.usesPlugins, emptyList(), libs, build.includeConfigurationCacheProblems)
                )
                RootProjectSpec(build.rootDir, children, emptyList(), build.producesPlugins, emptyList(), build.includeConfigurationCacheProblems)
            }
        }
    }
}