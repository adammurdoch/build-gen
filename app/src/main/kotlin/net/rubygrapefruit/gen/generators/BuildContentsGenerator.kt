package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.builders.RootProjectBuilder
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
        val builder = RootProjectBuilder(build)
        when (build.projects) {
            ProjectGraphSpec.RootProject -> {
                builder.root {
                    requiresPlugins(build.usesPlugins)
                    producesExternalLibrary(build.producesLibrary)
                }
            }
            ProjectGraphSpec.AppAndLibraries -> {
                val library = builder.child("util") {
                    requiresPlugins(build.usesPlugins)
                    producesExternalLibrary(build.producesLibrary)
                }
                builder.child("app") {
                    requiresPlugins(build.usesPlugins)
                    requiresExternalLibraries(build.usesLibraries)
                    requiresLibrary(library)
                }
            }
            ProjectGraphSpec.Libraries -> {
                val library = builder.child("impl") {
                    requiresPlugins(build.usesPlugins)
                    requiresExternalLibraries(build.usesLibraries)
                }
                builder.child("core") {
                    requiresPlugins(build.usesPlugins)
                    requiresLibrary(library)
                    producesExternalLibrary(build.producesLibrary)
                }
            }
        }
        return builder.build()
    }
}