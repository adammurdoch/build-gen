package net.rubygrapefruit.gen.generators

/**
 * Implementations must be thread safe.
 */
interface Generator<T> {
    fun generate(model: T)
}