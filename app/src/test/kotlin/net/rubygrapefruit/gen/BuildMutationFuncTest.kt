package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.templates.BuildLogic
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.ProductionBuildTreeStructure
import org.junit.Test

class BuildMutationFuncTest : AbstractFuncTest() {
    @Test
    fun `can regenerate an existing build`() {
        val dir = testDir.newFolder()

        generate(dir, ProductionBuildTreeStructure.MainBuild, BuildLogic.BuildSrc, Implementation.Custom)
        application(dir)
        runBuild(dir, "assemble")

        generate(dir, ProductionBuildTreeStructure.ChildBuilds, BuildLogic.ChildBuild, Implementation.Custom)
        application(dir)
        runBuild(dir, "assemble")
    }
}