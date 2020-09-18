package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.BuildSpec

class BuildContentsGenerator(
        private val scriptGenerator: ScriptGenerator,
        private val assemblers: List<Assembler<BuildContentsBuilder>>
) {
    fun buildContents(): Generator<BuildSpec> = Generator.of { generationContext ->
        val settings = scriptGenerator.settings(rootDir)
        settings.apply {
            for (childBuild in childBuilds) {
                includeBuild(rootDir.relativize(childBuild.rootDir).toString())
            }
        }

        val rootBuildScript = scriptGenerator.build(rootDir)
        rootBuildScript.apply {
            for (plugin in usesPlugins) {
                plugin(plugin.id)
            }
        }

        val builder = BuildContentsBuilder(this, settings, rootBuildScript)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }

        rootBuildScript.complete()
        settings.complete()
    }
}