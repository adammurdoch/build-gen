package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.TemplateOption
import java.io.File
import kotlin.test.Test

class NoBuildLogicFuncTest : AbstractFuncTest() {
    fun generate(template: BuildTreeTemplate, templateOptions: List<TemplateOption> = emptyList(), dsl: DslLanguage = DslLanguage.GroovyDsl): File {
        return generate(template.productionBuildTreeStructure, template.buildLogic, Implementation.None, templateOptions, dsl)
    }

    @Test
    fun canGenerateBuildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.mainBuildNoBuildLogic)
        buildTree(dir)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateBuildWithKotlinDsl() {
        val dir = generate(BuildTreeTemplate.mainBuildNoBuildLogic, dsl = DslLanguage.KotlinDsl)
        buildTree(dir, dsl = DslLanguage.KotlinDsl)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateLargeBuildWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.mainBuildNoBuildLogic, templateOptions = listOf(TemplateOption.largeBuild))
        buildTree(dir)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateTreeWithGroovyDsl() {
        val dir = generate(BuildTreeTemplate.childBuildsNoBuildLogic)
        buildTree(dir)
        runBuild(dir, "help")
    }
}