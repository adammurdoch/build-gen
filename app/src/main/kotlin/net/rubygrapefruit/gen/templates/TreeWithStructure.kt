package net.rubygrapefruit.gen.templates

class TreeWithStructure(
    val buildLogic: BuildLogic,
    val implementationOptions: List<TreeWithImplementation>
) {
    override fun toString(): String {
        return buildLogic.displayName
    }
}