package net.rubygrapefruit.gen.generators

import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class ParallelGenerationContext : GenerationContext, Closeable {
    private val executor = Executors.newFixedThreadPool(4)

    override fun <T> apply(models: Iterable<T>, generator: Generator<T>) {
        apply(models) { generator.generate(it, this) }
    }

    override fun <T> apply(model: T, generators: Iterable<Generator<T>>) {
        apply(generators) { it.generate(model, this) }
    }

    private fun <T> apply(elements: Iterable<T>, action: (T) -> Unit) {
        val results = mutableListOf<Future<*>>()
        for (element in elements) {
            val future = executor.submit {
                action(element)
            }
            results.add(future)
        }
        for (result in results) {
            result.get()
        }
    }

    override fun close() {
        executor.shutdown()
        executor.awaitTermination(2, TimeUnit.MINUTES)
    }
}