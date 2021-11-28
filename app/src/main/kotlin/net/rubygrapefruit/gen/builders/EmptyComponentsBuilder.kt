package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.BaseName
import net.rubygrapefruit.gen.specs.EmptyComponentProductionSpec
import net.rubygrapefruit.gen.specs.NameProvider

class EmptyComponentsBuilder(
    private val projectNames: NameProvider
) : ComponentsBuilder<EmptyComponentProductionSpec>() {
    override fun calculateContents(count: Int): List<EmptyComponentProductionSpec> {
        val result = mutableListOf<EmptyComponentProductionSpec>()
        for (i in 0 until count) {
            result.add(EmptyComponentProductionSpec(BaseName(projectNames.next())))
        }
        return result
    }
}