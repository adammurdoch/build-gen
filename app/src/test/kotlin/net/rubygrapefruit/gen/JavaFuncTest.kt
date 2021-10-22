package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.TemplateOption
import kotlin.test.Test

class JavaFuncTest : AbstractLanguageFuncTest(Implementation.Java) {
    @Test
    fun canGenerateTreeWithBuildLogicAndLibraryInChildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.childBuildsWithPluginChildBuildAndSharedLibrary)
        application(dir)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicAndLibraryInChildWithConfigurationCacheProblemsAndKotlinDsl() {
        val dir = generate(BuildTreeTemplate.childBuildsWithPluginChildBuildAndSharedLibrary, templateOptions = listOf(TemplateOption.configurationCacheProblems), dsl = DslLanguage.KotlinDsl)
        application(dir, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateToolingApiClient() {
        val dir = generate(BuildTreeTemplate.mainBuildWithBuildSrc, templateOptions = listOf(TemplateOption.toolingApiClient))
        application(dir)
        runBuild(dir.resolve("tooling-api"), "run")
    }
}