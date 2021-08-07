package net.rubygrapefruit.gen.templates

class TreeWithProductionStructure(
    val productionStructure: ProductionBuildTreeStructure,
    val buildLogicOptions: List<TreeWithStructure>
) {
    override fun toString(): String {
        return productionStructure.displayName
    }
}