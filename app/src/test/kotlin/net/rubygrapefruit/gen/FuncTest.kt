package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Theme
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test

class FuncTest {
    val rootDir = File("build/test/gen")
    val testDir = TemporaryFolder(rootDir)

    @BeforeTest
    fun setup() {
        rootDir.mkdirs()
        testDir.create()
    }

    @Test
    fun canGenerateMainBuildOnlyWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.MainBuildOnly, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateMainBuildOnlyWithKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.MainBuildOnly, Theme.None, DslLanguage.KotlinDsl)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateMainBuildWithBuildSrcWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildSrc, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicInChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.TreeWithBuildLogicChildBuild, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateMainBuildWithBuildLogicInBuildSrcAndChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuildAndBuildSrc, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateMainBuildWithBuildLogicInBuildSrcAndChildWithKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuildAndBuildSrc, Theme.None, DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateMainBuildWithBuildLogicInBuildSrcAndChildWithConfigurationCacheProblemsAndKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuildAndBuildSrc, Theme.ConfigurationCacheProblems, DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
    }

    fun runBuild(dir: File, vararg args: String) {
        val runner = GradleRunner.create()
        runner.withProjectDir(dir)
        runner.withArguments(args.toList())
        runner.forwardOutput()

        println("Running build ...")
        runner.build()
    }
}