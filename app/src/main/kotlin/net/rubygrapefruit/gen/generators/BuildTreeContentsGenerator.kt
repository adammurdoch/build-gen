package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.BuildTreeSpec

class BuildTreeContentsGenerator(
    private val buildGenerator: Generator<BuildSpec>,
    private val reportGenerator: Generator<BuildTreeSpec>
) : Generator<BuildTreeSpec> {
    override fun generate(model: BuildTreeSpec, generationContext: GenerationContext) {
        generationContext.generateInParallel(model.builds, buildGenerator)
        reportGenerator.generate(model, generationContext)
    }
}