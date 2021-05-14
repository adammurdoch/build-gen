package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import java.io.File
import kotlin.test.Test

class JavaFuncTest : AbstractFuncTest() {
    fun generate(dir: File, template: BuildTreeTemplate, theme: Theme = Theme.None, dsl: DslLanguage = DslLanguage.GroovyDsl) {
        generate(dir.toPath(), template, Implementation.Java, theme, dsl)
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.MainBuildWithBuildSrc)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.MainBuildWithPluginChildBuild)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.MainBuildWithBuildSrcAndPluginChildBuild)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicInChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.ChildBuildsWithPluginChildBuild)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.ChildBuildsWithBuildSrc)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithNestedChildBuildsWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.NestedChildBuildsWithPluginChildBuild)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicAndLibraryInChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.ChildBuildsWithPluginChildBuildAndSharedLibrary)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicAndLibraryInChildWithConfigurationCacheProblemsAndKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.ChildBuildsWithPluginChildBuildAndSharedLibrary, theme = Theme.ConfigurationCacheProblems, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
    }
}