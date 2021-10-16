package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.TemplateOption
import kotlin.test.Test

class JavaFuncTest : AbstractLanguageFuncTest(Implementation.Java) {
    @Test
    fun canGenerateTreeWithBuildLogicAndLibraryInChildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsWithPluginChildBuildAndSharedLibrary)
        application(dir)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicAndLibraryInChildWithConfigurationCacheProblemsAndKotlinDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsWithPluginChildBuildAndSharedLibrary, templateOptions = listOf(TemplateOption.ConfigurationCacheProblems), dsl = DslLanguage.KotlinDsl)
        application(dir, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
    }
}