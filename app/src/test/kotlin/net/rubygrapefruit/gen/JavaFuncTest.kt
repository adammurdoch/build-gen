package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import kotlin.test.Test

class JavaFuncTest: AbstractFuncTest() {
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
}