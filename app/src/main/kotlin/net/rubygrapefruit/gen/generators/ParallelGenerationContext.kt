package net.rubygrapefruit.gen.generators

import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class ParallelGenerationContext : GenerationContext, Closeable {
    private val executor = Executors.newCachedThreadPool()

    override fun <T> apply(models: Collection<T>, generator: Generator<T>) {
        apply(models) {
            generator.generate(it, this)
        }
    }

    override fun <T> apply(model: T, generators: Collection<Generator<T>>) {
        apply(generators) {
            it.generate(model, this)
        }
    }

    private fun <T> apply(elements: Collection<T>, action: (T) -> Unit) {
        if (elements.isEmpty()) {
            return
        }

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