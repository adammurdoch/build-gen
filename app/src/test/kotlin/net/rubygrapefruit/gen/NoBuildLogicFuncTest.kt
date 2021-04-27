package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import kotlin.test.Test

class NoBuildLogicFuncTest : AbstractFuncTest() {
    @Test
    fun canGenerateBuildWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.MainBuildOnly, Implementation.None, Theme.None, DslLanguage.GroovyDsl)
        runBuild(dir, "help")
    }

    @Test
    fun canGenerateBuildWithKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.MainBuildOnly, Implementation.None, Theme.None, DslLanguage.KotlinDsl)
        runBuild(dir, "help")
    }
}