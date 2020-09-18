package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder

class ConfigurationCacheProblemGenerator {
    fun build(): Assembler<BuildContentsBuilder> = Assembler.of {
        if (spec.includeConfigurationCacheProblems) {
            settingsScript.apply {
                block("gradle.buildFinished")
                method("System.getProperty(\"build.input\")")
            }
            rootBuildScript.apply {
                block("gradle.buildFinished")
                method("System.getProperty(\"build.input\")")
                for (plugin in spec.usesPlugins) {
                    block("tasks.named(\"${plugin.workerTaskName}\")") {
                        block("doLast") {
                            method("taskDependencies")
                        }
                    }
                }
            }
        }
    }

    fun plugin(): Assembler<PluginGenerationContext> = Assembler.of {
        if (build.includeConfigurationCacheProblems) {
            source.applyMethodBody("project.getGradle().buildFinished(r -> {});")
            source.applyMethodBody("System.getProperty(\"build.input\");")

            source.taskMethodBody("getTaskDependencies();")
        }
    }
}