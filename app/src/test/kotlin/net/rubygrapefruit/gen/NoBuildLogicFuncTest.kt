package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import java.io.File
import kotlin.test.Test

class NoBuildLogicFuncTest : AbstractFuncTest() {
    fun generate(dir: File, template: BuildTreeTemplate, theme: Theme = Theme.None, dsl: DslLanguage = DslLanguage.GroovyDsl) {
        generate(dir.toPath(), template, Implementation.None, theme, dsl, GeneratedDirectoryContentsSynchronizer())
    }

    @Test
    fun canGenerateBuildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.MainBuildNoBuildLogic)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateBuildWithKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.MainBuildNoBuildLogic, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateTreeWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir, BuildTreeTemplate.ChildBuildsNoBuildLogic)
        runBuild(dir, "help")
    }
}