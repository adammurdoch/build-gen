package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.ProjectGraphSpec
import net.rubygrapefruit.gen.specs.ProjectSpec
import net.rubygrapefruit.gen.specs.RootProjectSpec
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
        return build.projects {
            when (build.projects) {
                ProjectGraphSpec.RootProject -> {
                    root {
                        requiresPlugins(build.usesPlugins)
                        producesLibrary(build.producesLibrary)
                    }
                }
                ProjectGraphSpec.AppAndLibraries -> {
                    val library = project("util") {
                        requiresPlugins(build.usesPlugins)
                        producesLibrary(build.producesLibrary)
                    }
                    project("app") {
                        requiresPlugins(build.usesPlugins)
                        requiresLibraries(build.usesLibraries)
                        requiresLibrary(library)
                    }
                }
                ProjectGraphSpec.Libraries -> {
                    val library = project("impl") {
                        requiresPlugins(build.usesPlugins)
                    }
                    project("core") {
                        requiresPlugins(build.usesPlugins)
                        requiresLibraries(build.usesLibraries)
                        requiresLibrary(library)
                        producesLibrary(build.producesLibrary)
                    }
                }
            }
        }
    }
}