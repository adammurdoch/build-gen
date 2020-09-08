package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.BuildTreeSpec

class BuildTreeGenerator(private val buildGenerator: Generator<BuildSpec>) {
    fun generate(buildTree: BuildTreeSpec) {
        for (build in buildTree.builds) {
            buildGenerator.generate(build)
        }
    }
}