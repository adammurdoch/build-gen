package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.ExternalLibraryCoordinates
import net.rubygrapefruit.gen.specs.LocalLibraryCoordinates
import net.rubygrapefruit.gen.specs.ProjectSpec
import java.nio.file.Files

class ProjectContentsGenerator(
    private val scriptGenerator: ScriptGenerator,
    private val assemblers: List<Assembler<ProjectContentsBuilder>>
) {
    fun projectContents(): Generator<ProjectSpec> = Generator.of { generationContext ->
        Files.createDirectories(projectDir)

        val buildScript = scriptGenerator.build(projectDir)
        buildScript.apply {
            for (plugin in usesPlugins) {
                plugin(plugin.id)
            }
            if (usesPlugins.isNotEmpty()) {
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
        }

        val builder = ProjectContentsBuilder(this, buildScript)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }

        buildScript.complete()
    }
}