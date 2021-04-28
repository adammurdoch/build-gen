package net.rubygrapefruit.gen.specs

sealed class PluginImplementationSpec(
    val project: ProjectSpec,
    open val spec: PluginProductionSpec,
) {
    val pluginImplementationClass: JvmClassName
        get() = spec.className("PluginImpl")
}

class CustomPluginImplementationSpec(
    project: ProjectSpec,
    override val spec: CustomPluginProductionSpec,
) : PluginImplementationSpec(project, spec) {
    val taskImplementationClass: JvmClassName
        get() = spec.className("WorkerTask")
}

class JavaConventionPluginImplementationSpec(
    project: ProjectSpec,
    override val spec: JavaConventionPluginProductionSpec
) : PluginImplementationSpec(project, spec) {
}