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
            val hasProductionCode = build.producesLibraries.isNotEmpty() || build.producesApps.isNotEmpty()
            if (build.producesPlugins.isNotEmpty() && !hasProductionCode) {
                // Produces plugins and not libraries -> root project contains plugin
                root {
                    producesPlugins(build.producesPlugins)
                }
            } else if (hasProductionCode) {
                // Produces libraries and maybe plugins too
                val internalLibrary = project(build.projectNames.next()) {
                    requiresPlugins(build.usesPlugins)
                    producesLibrary()
                }
                for (library in build.producesLibraries) {
                    project(library.coordinates.name) {
                        requiresPlugins(build.usesPlugins)
                        if (build.topLevelLibraries.contains(library)) {
                            requiresExternalLibraries(build.usesLibraries)
                        }
                        requiresLibrary(internalLibrary)
                        producesLibrary(library)
                    }
                }
                for (app in build.producesApps) {
                    project(app.baseName.camelCase) {
                        requiresPlugins(build.usesPlugins)
                        requiresExternalLibraries(build.usesLibraries)
                        requiresLibrary(internalLibrary)
                    }
                }
                if (build.producesPlugins.isNotEmpty()) {
                    project("plugins") {
                        producesPlugins(build.producesPlugins)
                    }
                }
            }
        }
    }
}