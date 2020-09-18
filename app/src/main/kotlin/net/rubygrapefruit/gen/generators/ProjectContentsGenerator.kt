package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.ProjectContentsBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
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
        }

        val builder = ProjectContentsBuilder(this, buildScript)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }

        buildScript.complete()
    }
}