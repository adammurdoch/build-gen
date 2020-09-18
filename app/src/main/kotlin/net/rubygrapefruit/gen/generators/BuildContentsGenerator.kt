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

        val projects = projects(this)
        val subprojects = projects.filterIsInstance(SubProjectSpec::class.java)

        val settings = scriptGenerator.settings(rootDir)
        settings.apply {
            for (project in subprojects) {
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

        generationContext.apply(projects, projectGenerator)
    }

    private fun projects(build: BuildSpec): List<ProjectSpec> {
        return when (build.projects) {
            ProjectGraphSpec.RootProject -> listOf(
                    RootProjectSpec(build.rootDir, build.usesPlugins, build.producesPlugins, build.includeConfigurationCacheProblems)
            )
            ProjectGraphSpec.MultipleProjects -> listOf(
                    RootProjectSpec(build.rootDir, emptyList(), build.producesPlugins, build.includeConfigurationCacheProblems),
                    SubProjectSpec("app", build.rootDir.resolve("app"), build.usesPlugins, emptyList(), build.includeConfigurationCacheProblems),
                    SubProjectSpec("lib", build.rootDir.resolve("lib"), build.usesPlugins, emptyList(), build.includeConfigurationCacheProblems)
            )
        }
    }
}