package net.rubygrapefruit.gen.templates

class ParametersWithProductionStructure(
    val productionStructure: ProductionTreeStructure,
    val options: List<ParametersWithBuildLogic>
) {
    override fun toString(): String {
        return productionStructure.displayName
    }
}