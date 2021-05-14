package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import kotlin.test.Test

class JavaFuncTest : AbstractFuncTest() {
    @Test
    fun canGenerateBuildWithBuildLogicInBuildSrcWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildSrc, Implementation.Java, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicInChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.TreeWithBuildLogicChildBuild, Implementation.Java, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithNestedChildBuildsWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.TreeWithNestedChildBuild, Implementation.Java, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicAndLibraryInChildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.TreeWithBuildLogicAndLibraryChildBuild, Implementation.Java, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "assemble")
    }

    @Test
    fun canGenerateTreeWithBuildLogicAndLibraryInChildWithConfigurationCacheProblemsAndKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.TreeWithBuildLogicAndLibraryChildBuild, Implementation.Java, Theme.ConfigurationCacheProblems, DslLanguage.KotlinDsl)
        runBuild(dir, "assemble")
    }
}