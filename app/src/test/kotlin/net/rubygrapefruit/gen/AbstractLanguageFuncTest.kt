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

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrc)

        val app = application(dir)
        app.assertHasBuildSrc()

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInChildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithPluginChildBuild)

        val app = application(dir)
        app.assertNoBuildSrc()
        app.assertHasPluginsBuild()

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrcAndPluginChildBuild)

        val app = application(dir)
        app.assertHasBuildSrc()
        app.assertHasPluginsBuild()

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithKotlinDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrcAndPluginChildBuild, dsl = DslLanguage.KotlinDsl)

        val app = application(dir, dsl = DslLanguage.KotlinDsl)
        app.assertHasBuildSrc()
        app.assertHasPluginsBuild()

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcAndChildWithConfigurationCacheProblemsAndKotlinDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrcAndPluginChildBuild, theme = Theme.ConfigurationCacheProblems, dsl = DslLanguage.KotlinDsl)

        val app = application(dir, dsl = DslLanguage.KotlinDsl)
        app.assertHasBuildSrc()

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicInChildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsWithPluginChildBuild)

        application(dir)

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsWithBuildSrc)

        val app = application(dir)
        app.assertHasBuildSrc()

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithNestedChildBuildsWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.NestedChildBuildsWithPluginChildBuild)

        application(dir)

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithCyclicChildBuildsWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.CyclicChildBuildsWithPluginChildBuild)

        application(dir)

        runBuild(dir, "assemble")
    }
}