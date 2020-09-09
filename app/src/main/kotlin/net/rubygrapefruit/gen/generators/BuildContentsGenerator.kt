package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.BuildScriptBuilder
import net.rubygrapefruit.gen.files.ScriptGenerator
import net.rubygrapefruit.gen.files.SettingsScriptBuilder
import net.rubygrapefruit.gen.specs.BuildSpec

class BuildContentsGenerator(
        private val scriptGenerator: ScriptGenerator,
        private val generators: List<BuildGenerator>
) : Generator<BuildSpec> {
    override fun generate(model: BuildSpec) {
        val settings = scriptGenerator.settings(model.rootDir)
        settings.apply {
            for (childBuild in model.childBuilds) {
                includeBuild(model.rootDir.relativize(childBuild.rootDir).toString())
            }
        }

        val buildScript = scriptGenerator.build(model.rootDir)
        buildScript.apply {
            for (plugin in model.usesPlugins) {
                plugin(plugin.id)
            }
        }

        val context = BuildGenerationContextImpl(model, settings, buildScript)
        for (generator in generators) {
            generator.generate(context)
        }

        buildScript.complete()
        settings.complete()
    }

    private class BuildGenerationContextImpl(
            override val spec: BuildSpec,
            override val settingsScript: SettingsScriptBuilder,
            override val rootBuildScript: BuildScriptBuilder
    ) : BuildGenerationContext
}