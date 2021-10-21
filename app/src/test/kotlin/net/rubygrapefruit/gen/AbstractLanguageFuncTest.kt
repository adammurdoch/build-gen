package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.TemplateOption
import java.io.File
import kotlin.test.Test

abstract class AbstractLanguageFuncTest(private val implementation: Implementation) : AbstractFuncTest() {

    fun generate(template: BuildTreeTemplate, templateOptions: List<TemplateOption> = emptyList(), dsl: DslLanguage = DslLanguage.GroovyDsl): File {
        val dir = testDir.newFolder()
        generate(dir.toPath(), template, implementation, templateOptions, dsl, GeneratedDirectoryContentsSynchronizer())
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
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrcAndPluginChildBuild, templateOptions = listOf(TemplateOption.configurationCacheProblems), dsl = DslLanguage.KotlinDsl)

        val app = application(dir, dsl = DslLanguage.KotlinDsl)
        app.assertHasBuildSrc()

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateLargeBuildWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildWithBuildSrc, templateOptions = listOf(TemplateOption.largeBuild))

        val app = application(dir)
        app.assertHasBuildSrc()

        runBuild(dir, "help")
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
        val dir = generate(BuildTreeTemplate.ChildBuildsWithCycleAndPluginChildBuild)

        application(dir)

        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithChildBuildsThatUseMainBuildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsUseMainBuildAndWithPluginChildBuild)

        application(dir)

        runBuild(dir, "assemble")
    }
}