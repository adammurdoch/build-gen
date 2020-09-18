package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.BuildTreeSpec

class BuildTreeContentsGenerator(private val buildGenerator: Generator<BuildSpec>) : Generator<BuildTreeSpec> {
    override fun generate(model: BuildTreeSpec, generationContext: GenerationContext) {
        generationContext.apply(model.builds, buildGenerator)
    }
}