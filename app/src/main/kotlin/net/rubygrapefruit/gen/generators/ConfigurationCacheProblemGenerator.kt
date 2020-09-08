package net.rubygrapefruit.gen.generators

class ConfigurationCacheProblemGenerator : Generator<BuildGenerationContext> {
    override fun generate(model: BuildGenerationContext) {
        if (model.spec.includeConfigurationCacheProblems) {
            model.settingsScript.apply {
                block("gradle.buildFinished")
                method("System.getProperty(\"build.input\")")
            }
            model.rootBuildScript.apply {
                block("gradle.buildFinished")
                method("System.getProperty(\"build.input\")")
            }
        }
    }
}