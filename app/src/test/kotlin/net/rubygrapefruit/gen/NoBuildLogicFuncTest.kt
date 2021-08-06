package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import java.io.File
import kotlin.test.Test

class NoBuildLogicFuncTest : AbstractFuncTest() {
    fun generate(template: BuildTreeTemplate, theme: Theme = Theme.None, dsl: DslLanguage = DslLanguage.GroovyDsl): File {
        val dir = testDir.newFolder()
        generate(dir.toPath(), template, Implementation.None, theme, dsl, GeneratedDirectoryContentsSynchronizer())
        return dir
    }

    @Test
    fun canGenerateBuildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildNoBuildLogic)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateBuildWithKotlinDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildNoBuildLogic, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateTreeWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsNoBuildLogic)
        runBuild(dir, "help")
    }
}