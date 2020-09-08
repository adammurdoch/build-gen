package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.BuildSpec
import java.nio.file.Files

class BuildGenerator(private val scriptGenerator: ScriptGenerator) {
    fun generate(build: BuildSpec) {
        Files.createDirectories(build.rootDir)

        scriptGenerator.settings(build.rootDir) {
            for (childBuild in build.childBuilds) {
                includeBuild(build.rootDir.relativize(childBuild.rootDir).toString())
            }
        }

        scriptGenerator.build(build.rootDir) {
            for (plugin in build.requiresPlugins) {
                plugin(plugin.id)
            }
            if (build.producesPlugins.isNotEmpty()) {
                plugin("java-gradle-plugin")
                block("gradlePlugin") {
                    block("plugins") {
                        build.producesPlugins.forEachIndexed { index, plugin ->
                            block("plugin$index") {
                                property("id", plugin.id)
                                property("implementationClass", plugin.id)
                            }
                        }
                    }
                }
            }
        }
    }
}