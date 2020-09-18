package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.BuildSpec
import net.rubygrapefruit.gen.specs.BuildTreeSpec
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class BuildTreeContentsGenerator(private val buildGenerator: Generator<BuildSpec>) : Generator<BuildTreeSpec> {
    override fun generate(model: BuildTreeSpec) {
        val executor = Executors.newFixedThreadPool(4)
        try {
            for (build in model.builds) {
                executor.submit {
                    buildGenerator.generate(build)
                }
            }
            executor.shutdown()
            executor.awaitTermination(2, TimeUnit.MINUTES)
        } finally {
            executor.shutdownNow()
        }
    }
}