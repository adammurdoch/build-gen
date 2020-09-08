package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.BuildSpec
import java.nio.file.Files

class BuildGenerator(private val scriptGenerator: ScriptGenerator) {
    fun generate(build: BuildSpec) {
        Files.createDirectories(build.rootDir)

        scriptGenerator.settings(build.rootDir.resolve("settings.gradle")) {
            for (childBuild in build.childBuilds) {
                includeBuild(build.rootDir.relativize(childBuild.rootDir).toString())
            }
        }

        scriptGenerator.build(build.rootDir.resolve("build.gradle")) {
            for (plugin in build.requiresPlugins) {
                plugin(plugin.id)
            }
        }
    }
}