package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.BuildComponentProductionSpec

abstract class MultipleComponentsBuilder<T : BuildComponentProductionSpec, B : ComponentsBuilder<T>>(
) : ComponentsBuilder<T>() {
    private val builders = mutableListOf<B>()

    override val currentSize: Int
        get() = builders.fold(0) { i, v -> i + v.currentSize }

    fun add(): B {
        assertNotFinalized()
        val builder = createBuilder()
        builders.add(builder)
        return builder
    }

    protected abstract fun createBuilder(): B

    override fun calculateContents(): List<T> {
        return builders.flatMap { it.contents }
    }
}