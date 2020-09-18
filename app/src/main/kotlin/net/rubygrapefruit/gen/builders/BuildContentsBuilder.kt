package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.files.SettingsScriptBuilder
import net.rubygrapefruit.gen.specs.BuildSpec

class BuildContentsBuilder(
        val spec: BuildSpec,
        val settingsScript: SettingsScriptBuilder
)