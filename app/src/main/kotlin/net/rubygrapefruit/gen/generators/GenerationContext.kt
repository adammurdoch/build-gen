package net.rubygrapefruit.gen.generators

interface GenerationContext {
    /**
     * Applies the given generator to the given elements in parallel.
     */
    fun <T> apply(models: Iterable<T>, generator: Generator<T>)

    /**
     * Applies the given model to the given generators in parallel.
     */
    fun <T> apply(model: T, generators: Iterable<Generator<T>>)
}