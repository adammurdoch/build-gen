package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import org.junit.Test

class BuildMutationFuncTest : AbstractFuncTest() {
    @Test
    fun `can regenerate an existing build`() {
        val dir = testDir.newFolder()

        generate(dir.toPath(), BuildTreeTemplate.MainBuildWithBuildSrc, Implementation.Custom, emptyList(), DslLanguage.GroovyDsl, GeneratedDirectoryContentsSynchronizer())
        application(dir)
        runBuild(dir, "assemble")

        generate(dir.toPath(), BuildTreeTemplate.ChildBuildsWithPluginChildBuild, Implementation.Custom, emptyList(), DslLanguage.GroovyDsl, GeneratedDirectoryContentsSynchronizer())
        application(dir)
        runBuild(dir, "assemble")
    }
}