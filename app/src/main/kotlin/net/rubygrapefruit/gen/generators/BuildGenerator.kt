package net.rubygrapefruit.gen.generators

interface BuildGenerator {
    fun generate(context: BuildGenerationContext)
}