package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.builders.ProjectBuilder
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
            val allPluginBundles = mutableListOf<PluginBundleProductionSpec>()
            var hasProductionCode = false

            build.visit(object : BuildComponentVisitor {
                override fun visitPlugin(pluginBundle: PluginBundleProductionSpec) {
                    allPluginBundles.add(pluginBundle)
                }

                override fun visitApp(app: AppProductionSpec) {
                    hasProductionCode = true
                    project(app.baseName.camelCase) {
                        producesApp(app.implementationSpec)
                        connectDependencies(app, projectForExternalLib, projectForInternalLib)
                    }
                }

                override fun visitLibrary(library: ExternalLibraryProductionSpec) {
                    hasProductionCode = true
                    project(library.coordinates.name) {
                        projectForExternalLib[library] = producesLibrary(library)
                        connectDependencies(library, projectForExternalLib, projectForInternalLib)
                    }
                }

                override fun visitInternalLibrary(library: InternalLibraryProductionSpec) {
                    hasProductionCode = true
                    project(library.baseName.camelCase) {
                        projectForInternalLib[library] = producesLibrary(library.spec)
                        connectDependencies(library, projectForExternalLib, projectForInternalLib)
                    }
                }

                override fun visitEmptyComponent(component: EmptyComponentProductionSpec) {
                    project(component.baseName.camelCase) {}
                }
            })

            if (allPluginBundles.isNotEmpty()) {
                if (allPluginBundles.size == 1 && !hasProductionCode) {
                    // Produces plugins and nothing else -> root project contains plugin
                    root {
                        val pluginBundle = allPluginBundles.first()
                        for (plugin in pluginBundle.plugins) {
                            producesPlugin(plugin)
                        }
                        connectDependencies(pluginBundle, projectForExternalLib, projectForInternalLib)
                    }
                } else {
                    for (pluginBundle in allPluginBundles) {
                        project(pluginBundle.baseName.camelCase) {
                            for (plugin in pluginBundle.plugins) {
                                producesPlugin(plugin)
                            }
                            connectDependencies(pluginBundle, projectForExternalLib, projectForInternalLib)
                        }
                    }
                }
            }
        }
    }

    private fun ProjectBuilder.connectDependencies(
        component: BuildComponentProductionSpec,
        projectForExternalLib: Map<ExternalLibraryProductionSpec, LibraryUseSpec>,
        projectForInternalLib: Map<InternalLibraryProductionSpec, LibraryUseSpec>
    ) {
        requiresPlugins(component.usesPlugins)
        requiresExternalLibraries(component.usesLibraries)
        for (required in component.usesLibrariesFromSameBuild) {
            requiresLibrary(projectForExternalLib.getValue(required))
        }
        for (required in component.usesImplementationLibraries) {
            requiresLibrary(projectForInternalLib.getValue(required))
        }
    }
}