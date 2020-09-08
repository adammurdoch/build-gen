package net.rubygrapefruit.gen.generators

interface Generator<T> {
    fun generate(model: T)
}