package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.FileGenerationContext
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.ExternalLibraryCoordinates
import net.rubygrapefruit.gen.specs.LocalLibraryCoordinates
import net.rubygrapefruit.gen.specs.ProjectSpec
import java.nio.file.Files

class ProjectContentsGenerator(
    private val scriptGenerator: ScriptGenerator,
    private val fileGenerationContext: FileGenerationContext,
    private val assembler: Assembler<ProjectContentsBuilder>
) {
    fun projectContents(): Generator<ProjectSpec> = Generator.of { generationContext ->
        Files.createDirectories(projectDir)
        fileGenerationContext.directoryToClean(projectDir.resolve("build"))

        val buildScript = scriptGenerator.build(projectDir)
        buildScript.apply {
            for (plugin in usesPlugins) {
                plugin(plugin.id)
            }
            if (producesLibrary?.externalCoordinates != null) {
                property("group", producesLibrary.externalCoordinates.group)
            }
            for (library in usesLibraries) {
                val coordinates = library.coordinates
                when (coordinates) {
                    is ExternalLibraryCoordinates -> implementationDependency(coordinates.group, coordinates.name, coordinates.version)
                    is LocalLibraryCoordinates -> implementationDependency(coordinates.producedByProject)
                }
            }
        }

        val builder = ProjectContentsBuilder(this, buildScript)
        assembler.assemble(builder, generationContext)

        buildScript.complete()
    }
}