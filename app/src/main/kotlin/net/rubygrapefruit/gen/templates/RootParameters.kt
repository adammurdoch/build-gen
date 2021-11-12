package net.rubygrapefruit.gen.templates

class RootParameters {
    val options: List<ParametersWithProductionStructure>
        get() = BuildTreeTemplate.productionStructures()
}