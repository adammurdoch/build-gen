package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.BaseName
import net.rubygrapefruit.gen.specs.EmptyComponentProductionSpec
import net.rubygrapefruit.gen.specs.NameProvider

class EmptyComponentsBuilder(
    private val projectNames: NameProvider
) : ComponentsBuilder<EmptyComponentProductionSpec>() {
    private var count = 0

    override val currentSize: Int
        get() = count

    fun add() {
        assertNotFinalized()
        count++
    }

    override fun calculateContents(): List<EmptyComponentProductionSpec> {
        val result = mutableListOf<EmptyComponentProductionSpec>()
        for (i in 0 until count) {
            result.add(EmptyComponentProductionSpec(BaseName(projectNames.next())))
        }
        return result
    }
}