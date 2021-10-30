package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.FileGenerationContext
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.*
import java.nio.file.Files

class BuildContentsGenerator(
    private val scriptGenerator: ScriptGenerator,
    private val fileGenerationContext: FileGenerationContext,
    private val buildAssembler: Assembler<BuildContentsBuilder>,
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
        buildAssembler.assemble(builder, generationContext)

        settings.complete()

        generationContext.generateInParallel(rootProject.projects, projectGenerator)
    }

    private fun projects(build: BuildSpec): RootProjectSpec {
        return build.projects {
            val projectForInternalLib = mutableMapOf<InternalLibraryProductionSpec, LibraryUseSpec>()
            val projectForExternalLib = mutableMapOf<ExternalLibraryProductionSpec, LibraryUseSpec>()
            val allPlugins = mutableListOf<PluginProductionSpec>()
            var hasProductionCode = false

            build.visit(object : BuildComponentVisitor {
                override fun visitPlugin(plugin: PluginProductionSpec) {
                    allPlugins.add(plugin)
                }

                override fun visitApp(app: AppProductionSpec) {
                    hasProductionCode = true
                    project(app.baseName.camelCase) {
                        producesApp(app.implementationSpec)
                        requiresPlugins(app.usesPlugins)
                        requiresExternalLibraries(app.usesLibraries)
                        for (required in app.usesImplementationLibraries) {
                            requiresLibrary(projectForInternalLib.getValue(required))
                        }
                    }
                }

                override fun visitLibrary(library: ExternalLibraryProductionSpec) {
                    hasProductionCode = true
                    project(library.coordinates.name) {
                        projectForExternalLib[library] = producesLibrary(library)
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

                override fun visitInternalLibrary(library: InternalLibraryProductionSpec) {
                    hasProductionCode = true
                    project(library.baseName.camelCase) {
                        requiresPlugins(library.usesPlugins)
                        projectForInternalLib[library] = producesLibrary(library.spec)
                    }
                }

                override fun visitEmptyComponent(component: EmptyComponentProductionSpec) {
                    project(component.baseName.camelCase) {}
                }
            })

            if (allPlugins.isNotEmpty() && !hasProductionCode) {
                // Produces plugins and nothing else -> root project contains plugin
                root {
                    for (plugin in allPlugins) {
                        producesPlugin(plugin)
                        requiresPlugins(plugin.usesPlugins)
                    }
                }
            } else if (allPlugins.isNotEmpty() && hasProductionCode) {
                project("plugins") {
                    for (plugin in allPlugins) {
                        producesPlugin(plugin)
                        requiresPlugins(plugin.usesPlugins)
                    }
                }
            }
        }
    }
}