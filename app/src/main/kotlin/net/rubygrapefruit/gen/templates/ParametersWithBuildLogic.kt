package net.rubygrapefruit.gen.templates

class ParametersWithBuildLogic(
    val buildLogic: BuildLogic,
    val options: List<Parameters>
) {
    override fun toString(): String {
        return buildLogic.displayName
    }
}