package net.rubygrapefruit.gen.generators

/**
 * Generates filesystem state from the given model. Implementations must be thread safe.
 */
interface Generator<in T> {
    fun generate(model: T, generationContext: GenerationContext)

    companion object {
        fun <T> of(body: T.(GenerationContext) -> Unit): Generator<T> {
            return object : Generator<T> {
                override fun generate(model: T, generationContext: GenerationContext) {
                    body(model, generationContext)
                }
            }
        }
    }
}