package net.rubygrapefruit.gen.builders

/**
 * A container that builds zero or more elements of type T
 */
abstract class ComponentsBuilder<T> : FinalizableBuilder<List<T>>() {

    abstract val currentSize: Int

    val contents: List<T>
        get() = finalized()
}