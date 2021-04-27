package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.BuildSpec
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
            val producesLibrary = build.producesLibrary
            val hasLibraries = build.usesPlugins.isNotEmpty()
            if (hasLibraries) {
                val library = project("impl") {
                    requiresPlugins(build.usesPlugins)
                }
                project("core") {
                    requiresPlugins(build.usesPlugins)
                    requiresLibraries(build.usesLibraries)
                    requiresLibrary(library)
                    producesLibrary(producesLibrary)
                }
                if (build.producesPlugins.isNotEmpty()) {
                    project("plugins") {
                        producesPlugins(build.producesPlugins)
                    }
                }
            } else if (producesLibrary != null) {
                project(producesLibrary.coordinates.name) {
                    producesLibrary(producesLibrary)
                }
                if (build.producesPlugins.isNotEmpty()) {
                    project("plugins") {
                        producesPlugins(build.producesPlugins)
                    }
                }
            } else {
                root {
                    producesPlugins(build.producesPlugins)
                }
            }
        }
    }
}