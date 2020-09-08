package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.BuildSpec

class BuildGenerator(private val scriptGenerator: ScriptGenerator, private val sourceFileGenerator: SourceFileGenerator) : Generator<BuildSpec> {
    override fun generate(build: BuildSpec) {
        scriptGenerator.settings(build.rootDir) {
            for (childBuild in build.childBuilds) {
                includeBuild(build.rootDir.relativize(childBuild.rootDir).toString())
            }
        }

        scriptGenerator.build(build.rootDir) {
            for (plugin in build.usesPlugins) {
                plugin(plugin.id)
            }
            if (build.producesPlugins.isNotEmpty()) {
                plugin("java-gradle-plugin")
                block("gradlePlugin") {
                    block("plugins") {
                        build.producesPlugins.forEachIndexed { index, plugin ->
                            namedItem("plugin$index") {
                                property("id", plugin.id)
                                property("implementationClass", plugin.implementationClass.name)
                            }
                        }
                    }
                }
            }
        }

        for (plugin in build.producesPlugins) {
            sourceFileGenerator.java(build.rootDir.resolve("src/main/java"), plugin.implementationClass) {
                imports("org.gradle.api.Plugin")
                imports("org.gradle.api.Project")
                implements("Plugin<Project>")
                method("public void apply(Project project) { System.out.println(\"apply ${plugin.id}\"); }")
            }
        }
    }
}