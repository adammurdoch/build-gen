package net.rubygrapefruit.gen.generators

class ConfigurationCacheProblemGenerator : BuildGenerator, PluginGenerator {
    override fun generate(context: BuildGenerationContext) {
        if (context.spec.includeConfigurationCacheProblems) {
            context.settingsScript.apply {
                block("gradle.buildFinished")
                method("System.getProperty(\"build.input\")")
            }
            context.rootBuildScript.apply {
                block("gradle.buildFinished")
                method("System.getProperty(\"build.input\")")
                for (plugin in context.spec.usesPlugins) {
                    block("tasks.named(\"${plugin.workerTaskName}\")") {
                        block("doLast") {
                            method("taskDependencies")
                        }
                    }
                }
            }
        }
    }

    override fun generate(context: PluginGenerationContext) {
        if (context.build.includeConfigurationCacheProblems) {
            context.source.applyMethodBody("project.getGradle().buildFinished(r -> {});")
            context.source.applyMethodBody("System.getProperty(\"build.input\");")

            context.source.taskMethodBody("getTaskDependencies();")
        }
    }
}