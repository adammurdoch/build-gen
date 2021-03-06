package net.rubygrapefruit.gen.generators

interface GenerationContext {
    /**
     * Applies the given generator to the given elements in parallel.
     */
    fun <T> generateInParallel(models: Collection<T>, generator: Generator<T>)

    /**
     * Applies the given generators to the given model in parallel.
     */
    fun <T> generateInParallel(model: T, generators: Collection<Generator<T>>)
}