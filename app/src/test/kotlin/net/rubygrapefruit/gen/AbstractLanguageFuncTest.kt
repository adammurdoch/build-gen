package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

abstract class AbstractLanguageFuncTest(private val implementation: Implementation) : AbstractFuncTest() {

    fun generate(template: BuildTreeTemplate, theme: Theme = Theme.None, dsl: DslLanguage = DslLanguage.GroovyDsl): File {
        val dir = testDir.newFolder()
        generate(dir.toPath(), template, implementation, theme, dsl, GeneratedDirectoryContentsSynchronizer())
        return dir
    }

    fun application(dir: File): ApplicationFixture {
        val appDir = dir.resolve("app")
        assertTrue(appDir.exists())
        return ApplicationFixture()
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrc)
        runBuild(dir, "assemble")
        application(dir)
    }

    @Test
    fun canGenerateBuildWithBuildLogicInChildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithPluginChildBuild)
        runBuild(dir, "assemble")
        application(dir)
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrcAndPluginChildBuild)
        runBuild(dir, "assemble")
        application(dir)
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithKotlinDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrcAndPluginChildBuild, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
        application(dir)
    }

    @Test
    fun canGenerateTreeWithBuildLogicInChildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsWithPluginChildBuild)
        runBuild(dir, "assemble")
        application(dir)
    }

    @Test
    fun canGenerateTreeWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsWithBuildSrc)
        runBuild(dir, "assemble")
        application(dir)
    }

    @Test
    fun canGenerateTreeWithNestedChildBuildsWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.NestedChildBuildsWithPluginChildBuild)
        runBuild(dir, "assemble")
        application(dir)
    }

    @Test
    fun canGenerateTreeWithCyclicChildBuildsWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.CyclicChildBuildsWithPluginChildBuild)
        runBuild(dir, "assemble")
        application(dir)
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithConfigurationCacheProblemsAndKotlinDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrcAndPluginChildBuild, theme = Theme.ConfigurationCacheProblems, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
        application(dir)
    }
}