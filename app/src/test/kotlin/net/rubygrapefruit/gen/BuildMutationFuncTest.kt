package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.BuildLogic
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.ProductionTreeStructure
import org.junit.Test

class BuildMutationFuncTest : AbstractFuncTest() {
    @Test
    fun `can generate into an existing build`() {
        val dir = testDir.newFolder()

        generate(dir, ProductionTreeStructure.MainBuild, BuildLogic.BuildSrc, Implementation.Custom)

        val appBefore = application(dir)
        appBefore.assertHasBuildSrc()
        runBuild(dir, "assemble")

        generate(dir, ProductionTreeStructure.ChildBuilds, BuildLogic.ChildBuild, Implementation.Custom, dsl = DslLanguage.KotlinDsl)

        val appAfter = application(dir, dsl = DslLanguage.KotlinDsl)
        appAfter.assertNoBuildSrc()
        runBuild(dir, "assemble")
    }

    @Test
    fun `can regenerate an existing build`() {
        val dir = testDir.newFolder()

        generate(dir, ProductionTreeStructure.MainBuild, BuildLogic.BuildSrc, Implementation.Custom, dsl = DslLanguage.KotlinDsl)

        val appBefore = application(dir, dsl = DslLanguage.KotlinDsl)
        appBefore.assertHasBuildSrc()
        runBuild(dir, "assemble")

        regenerate(dir.toPath(), GeneratedDirectoryContentsSynchronizer(dir.toPath()))

        val appAfter = application(dir, dsl = DslLanguage.KotlinDsl)
        appAfter.assertHasBuildSrc()
        runBuild(dir, "assemble")
    }
}