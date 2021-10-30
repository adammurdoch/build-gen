package net.rubygrapefruit.gen.generators

/**
 * Mutates a model, possibly generating file system state.
 *
 * Implementations must be thread safe (may act on multiple models concurrently)
 */
interface Assembler<T> {
    fun assemble(model: T, generationContext: GenerationContext)

    companion object {
        fun <T> of(body: T.(GenerationContext) -> Unit): Assembler<T> {
            return object : Assembler<T> {
                override fun assemble(model: T, generationContext: GenerationContext) {
                    body(model, generationContext)
                }
            }
        }

        fun <T> of(vararg assemblers: Assembler<T>): Assembler<T> {
            return object : Assembler<T> {
                override fun assemble(model: T, generationContext: GenerationContext) {
                    for (assembler in assemblers) {
                        assembler.assemble(model, generationContext)
                    }
                }
            }
        }
    }
}