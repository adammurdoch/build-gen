package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.builders.BuildContentsBuilder
import net.rubygrapefruit.gen.builders.PluginImplementationBuilder
import net.rubygrapefruit.gen.builders.ProjectContentsBuilder

class ConfigurationCacheProblemGenerator {
    fun buildContents(): Assembler<BuildContentsBuilder> = Assembler.of {
        if (spec.includeConfigurationCacheProblems) {
            settingsScript.apply {
                block("gradle.buildFinished")
                method("System.getProperty(\"build.input\")")
            }
        }
    }

    fun projectContents(): Assembler<ProjectContentsBuilder> = Assembler.of {
        if (spec.includeConfigurationCacheProblems) {
            buildScript.apply {
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

    fun pluginImplementation(): Assembler<PluginImplementationBuilder> = Assembler.of {
        if (includeConfigurationCacheProblems) {
            source.applyMethodBody {
                statements("project.getGradle().buildFinished(r -> {});")
                statements("System.getProperty(\"build.input\");")
            }
            source.taskMethodBody("getTaskDependencies();")
        }
    }
}