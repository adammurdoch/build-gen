package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import kotlin.test.Test

class CustomPluginFuncTest : AbstractFuncTest() {
    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildSrc, Implementation.Custom, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicInChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.TreeWithBuildLogicChildBuild, Implementation.Custom, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuild, Implementation.Custom, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuildAndBuildSrc, Implementation.Custom, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuildAndBuildSrc, Implementation.Custom, Theme.None, DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithConfigurationCacheProblemsAndKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuildAndBuildSrc, Implementation.Custom, Theme.ConfigurationCacheProblems, DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
    }
}