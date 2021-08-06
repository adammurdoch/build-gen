package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.FileGenerationContext
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.ProjectSpec
import net.rubygrapefruit.gen.specs.RootProjectSpec
import java.nio.file.Files

class BuildContentsGenerator(
    private val scriptGenerator: ScriptGenerator,
    private val fileGenerationContext: FileGenerationContext,
    private val assemblers: List<Assembler<BuildContentsBuilder>>,
    private val projectGenerator: Generator<ProjectSpec>
) {
    fun buildContents(): Generator<BuildSpec> = Generator.of { generationContext ->
        Files.createDirectories(rootDir)
        fileGenerationContext.directoryToClean(rootDir.resolve(".gradle"))

        val rootProject = projects(this)

        val settings = scriptGenerator.settings(rootDir)
        settings.apply {
            for (childBuild in childBuilds) {
                includeBuild(rootDir.relativize(childBuild.rootDir).toString())
            }
            for (project in rootProject.children) {
                includeProject(project.name)
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
            val internalLibraries = build.internalLibraries.map {
                project(build.projectNames.next()) {
                    requiresPlugins(build.usesPlugins)
                    producesLibrary()
                }
            }.filterNotNull()

            val producesLibraries = build.producesLibraries
            val hasLibraries = build.usesPlugins.isNotEmpty()
            if (hasLibraries) {
                val firstLibrary = producesLibraries.firstOrNull()
                val projectName = if (firstLibrary != null) firstLibrary.coordinates.name else build.projectNames.next()
                project(projectName) {
                    requiresPlugins(build.usesPlugins)
                    requiresExternalLibraries(build.usesLibraries)
                    requiresLibraries(internalLibraries)
                    producesLibrary(firstLibrary)
                }
                for (library in producesLibraries.drop(1)) {
                    project(library.coordinates.name) {
                        requiresPlugins(build.usesPlugins)
                        requiresLibraries(internalLibraries)
                        producesLibrary(library)
                    }
                }
                if (build.producesPlugins.isNotEmpty()) {
                    project("plugins") {
                        producesPlugins(build.producesPlugins)
                    }
                }
            } else if (producesLibraries.isNotEmpty()) {
                for (library in producesLibraries) {
                    project(library.coordinates.name) {
                        requiresLibraries(internalLibraries)
                        producesLibrary(library)
                    }
                }
                if (build.producesPlugins.isNotEmpty()) {
                    project("plugins") {
                        requiresLibraries(internalLibraries)
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