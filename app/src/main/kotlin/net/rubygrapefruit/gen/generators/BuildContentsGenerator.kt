package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.specs.BuildSpec

class BuildContentsGenerator(
        private val scriptGenerator: ScriptGenerator,
        private val assemblers: List<Assembler<BuildContentsBuilder>>
) : Generator<BuildSpec> {
    override fun generate(model: BuildSpec, generationContext: GenerationContext) {
        val settings = scriptGenerator.settings(model.rootDir)
        settings.apply {
            for (childBuild in model.childBuilds) {
                includeBuild(model.rootDir.relativize(childBuild.rootDir).toString())
            }
        }

        val rootBuildScript = scriptGenerator.build(model.rootDir)
        rootBuildScript.apply {
            for (plugin in model.usesPlugins) {
                plugin(plugin.id)
            }
        }

        val builder = BuildContentsBuilder(model, settings, rootBuildScript)
        for (assembler in assemblers) {
            assembler.assemble(builder, generationContext)
        }

        rootBuildScript.complete()
        settings.complete()
    }
}