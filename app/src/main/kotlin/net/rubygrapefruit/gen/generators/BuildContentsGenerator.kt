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
            val producesLibraries = build.producesLibraries
            val hasLibraries = build.usesPlugins.isNotEmpty()
            if (hasLibraries) {
                val internalLibrary = project(build.projectNames.next()) {
                    requiresPlugins(build.usesPlugins)
                    producesLibrary()
                }
                val firstLibrary = producesLibraries.firstOrNull()
                val projectName = if (firstLibrary != null) firstLibrary.coordinates.name else build.projectNames.next()
                project(projectName) {
                    requiresPlugins(build.usesPlugins)
                    requiresLibraries(build.usesLibraries)
                    requiresLibrary(internalLibrary)
                    producesLibrary(firstLibrary)
                }
                for (library in producesLibraries.drop(1)) {
                    project(library.coordinates.name) {
                        requiresPlugins(build.usesPlugins)
                        requiresLibrary(internalLibrary)
                        producesLibrary(library)
                    }
                }
                if (build.producesPlugins.isNotEmpty()) {
                    project("plugins") {
                        producesPlugins(build.producesPlugins)
                    }
                }
            } else if (producesLibraries.isNotEmpty()) {
                val internalLibrary = project(build.projectNames.next()) {
                    producesLibrary()
                }
                for (library in producesLibraries) {
                    project(library.coordinates.name) {
                        requiresLibrary(internalLibrary)
                        producesLibrary(library)
                    }
                }
                if (build.producesPlugins.isNotEmpty()) {
                    project("plugins") {
                        requiresLibrary(internalLibrary)
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