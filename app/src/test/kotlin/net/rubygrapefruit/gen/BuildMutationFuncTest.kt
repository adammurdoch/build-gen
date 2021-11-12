package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.templates.BuildLogic
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.ProductionTreeStructure
import org.junit.Test

class BuildMutationFuncTest : AbstractFuncTest() {
    @Test
    fun `can regenerate an existing build`() {
        val dir = testDir.newFolder()

        generate(dir, ProductionTreeStructure.MainBuild, BuildLogic.BuildSrc, Implementation.Custom)
        application(dir)
        runBuild(dir, "assemble")

        generate(dir, ProductionTreeStructure.ChildBuilds, BuildLogic.ChildBuild, Implementation.Custom)
        application(dir)
        runBuild(dir, "assemble")
    }
}