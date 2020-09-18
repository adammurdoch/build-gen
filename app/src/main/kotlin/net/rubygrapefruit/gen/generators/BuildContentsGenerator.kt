package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.ProjectGraphSpec
import net.rubygrapefruit.gen.specs.ProjectSpec
import java.nio.file.Files

class BuildContentsGenerator(
        private val scriptGenerator: ScriptGenerator,
        private val assemblers: List<Assembler<BuildContentsBuilder>>,
        private val projectGenerator: Generator<ProjectSpec>
) {
    fun buildContents(): Generator<BuildSpec> = Generator.of { generationContext ->
        Files.createDirectories(rootDir)

        val subprojects = subprojects(this)

        val settings = scriptGenerator.settings(rootDir)
        settings.apply {
            for (project in subprojects) {
                includeProject(project.name)
            }
            for (childBuild in childBuilds) {
                includeBuild(rootDir.relativize(childBuild.rootDir).toString())
            }
        }

        val rootBuildScript = scriptGenerator.build(rootDir)
        rootBuildScript.apply {
            for (plugin in usesPlugins) {
                plugin(plugin.id)
            }
        }

        val builder = BuildContentsBuilder(this, settings, rootBuildScript)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }

        rootBuildScript.complete()
        settings.complete()

        generationContext.apply(subprojects, projectGenerator)
    }

    private fun subprojects(build: BuildSpec): List<ProjectSpec> {
        return when (build.projects) {
            ProjectGraphSpec.RootProject -> emptyList<ProjectSpec>()
            ProjectGraphSpec.MultipleProjects -> listOf(
                    ProjectSpec("app", build.rootDir.resolve("app")),
                    ProjectSpec("lib", build.rootDir.resolve("lib"))
            )
        }
    }
}