package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.TemplateOption
import java.io.File
import kotlin.test.Test

class NoBuildLogicFuncTest : AbstractFuncTest() {
    fun generate(template: BuildTreeTemplate, templateOptions: List<TemplateOption> = emptyList(), dsl: DslLanguage = DslLanguage.GroovyDsl): File {
        val dir = testDir.newFolder()
        generate(dir.toPath(), template, Implementation.None, templateOptions, dsl, GeneratedDirectoryContentsSynchronizer())
        return dir
    }

    @Test
    fun canGenerateBuildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildNoBuildLogic)
        buildTree(dir)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateBuildWithKotlinDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildNoBuildLogic, dsl = DslLanguage.KotlinDsl)
        buildTree(dir, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateLargeBuildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.MainBuildNoBuildLogic, templateOptions = listOf(TemplateOption.largeBuild))
        buildTree(dir)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateTreeWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.ChildBuildsNoBuildLogic)
        buildTree(dir)
        runBuild(dir, "help")
    }
}