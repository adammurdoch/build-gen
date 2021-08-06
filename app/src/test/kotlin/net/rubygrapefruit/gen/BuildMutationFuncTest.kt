package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.Theme
import org.junit.Test

class BuildMutationFuncTest: AbstractFuncTest() {
    @Test
    fun `can regenerate an existing build`() {
        val dir = testDir.newFolder()

        generate(dir.toPath(), BuildTreeTemplate.MainBuildWithBuildSrc, Implementation.Custom, Theme.None, DslLanguage.GroovyDsl, GeneratedDirectoryContentsSynchronizer())
        runBuild(dir, "assemble")

        generate(dir.toPath(), BuildTreeTemplate.ChildBuildsWithPluginChildBuild, Implementation.Custom, Theme.None, DslLanguage.GroovyDsl, GeneratedDirectoryContentsSynchronizer())
        runBuild(dir, "assemble")
    }
}