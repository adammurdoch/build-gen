package net.rubygrapefruit.gen.specs

sealed class PluginImplementationSpec(
    val project: ProjectSpec,
    open val spec: PluginProductionSpec,
)

class CustomPluginImplementationSpec(
    project: ProjectSpec,
    override val spec: CustomPluginProductionSpec,
) : PluginImplementationSpec(project, spec) {
    val pluginImplementationClass: JvmClassName = spec.className("PluginImpl")
    val taskImplementationClass: JvmClassName = spec.className("WorkerTask")
}

class JavaConventionPluginImplementationSpec(project: ProjectSpec, override val spec: JavaConventionPluginProductionSpec) : PluginImplementationSpec(project, spec) {
}