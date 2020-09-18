package net.rubygrapefruit.gen.generators

/**
 * Generates filesystem state from the given model. Implementations must be thread safe.
 */
interface Generator<in T> {
    fun generate(model: T, generationContext: GenerationContext)
}