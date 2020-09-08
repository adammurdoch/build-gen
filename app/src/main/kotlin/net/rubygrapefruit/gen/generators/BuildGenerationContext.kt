package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.BuildScriptBuilder
import net.rubygrapefruit.gen.files.SettingsScriptBuilder
import net.rubygrapefruit.gen.specs.BuildSpec

interface BuildGenerationContext {
    val spec: BuildSpec
    val settingsScript: SettingsScriptBuilder
    val rootBuildScript: BuildScriptBuilder
}