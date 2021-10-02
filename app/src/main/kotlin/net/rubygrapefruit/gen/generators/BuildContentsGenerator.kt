package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.FileGenerationContext
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.*
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
            for (childBuild in includedBuilds) {
                if (rootDir == childBuild.rootDir) {
                    includeBuild(".")
                } else {
                    includeBuild(rootDir.relativize(childBuild.rootDir).toString())
                }
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

        generationContext.generateInParallel(rootProject.projects, projectGenerator)
    }

    private fun projects(build: BuildSpec): RootProjectSpec {
        return build.projects {
            val hasProductionCode = build.producesLibraries.isNotEmpty() || build.producesApps.isNotEmpty() || build.implementationLibraries.isNotEmpty()
            if (build.producesPlugins.isNotEmpty() && !hasProductionCode) {
                // Produces plugins and not libraries -> root project contains plugin
                root {
                    producesPlugins(build.producesPlugins)
                }
            } else if (hasProductionCode) {
                // Produces libraries and maybe plugins too

                // Implementation libraries
                val projectForInternalLib = mutableMapOf<InternalLibrarySpec, LibraryUseSpec>()
                for (library in build.implementationLibraries) {
                    project(library.baseName.camelCase) {
                        requiresPlugins(build.usesPlugins)
                        projectForInternalLib[library] = producesLibrary(library.spec)
                    }
                }

                // Exported libraries
                val projectForExternalLib = mutableMapOf<ExternalLibraryProductionSpec, LibraryUseSpec>()
                for (library in build.producesLibraries) {
                    project(library.coordinates.name) {
                        projectForExternalLib[library] = producesLibrary(library)
                    }
                }
                for (library in build.producesLibraries) {
                    project(library.coordinates.name) {
                        requiresPlugins(library.usesPlugins)
                        requiresExternalLibraries(library.usesLibraries)
                        for (required in library.usesLibrariesFromSameBuild) {
                            requiresLibrary(projectForExternalLib.getValue(required))
                        }
                        for (required in library.usesImplementationLibraries) {
                            requiresLibrary(projectForInternalLib.getValue(required))
                        }
                    }
                }

                // Apps
                for (app in build.producesApps) {
                    project(app.baseName.camelCase) {
                        requiresPlugins(app.usesPlugins)
                        requiresExternalLibraries(app.usesLibraries)
                        for (required in app.usesImplementationLibraries) {
                            requiresLibrary(projectForInternalLib.getValue(required))
                        }
                    }
                }

                // Plugins
                if (build.producesPlugins.isNotEmpty()) {
                    project("plugins") {
                        producesPlugins(build.producesPlugins)
                    }
                }
            }
        }
    }
}