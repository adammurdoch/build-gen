package net.rubygrapefruit.gen.specs

sealed class PluginProductionSpec(
    private val baseName: String,
    val id: String,
) {
    val workerTaskName: String
        get() = identifier("worker")

    val lifecycleTaskName: String
        get() = baseName

    /**
     * Creates a unique identifier based on the identity of this plugin.
     */
    fun identifier(suffix: String) = baseName + suffix.capitalize()

    /**
     * Creates a unique fully-qualified class name based on the identity of this plugin.
     */
    fun className(classNameSuffix: String): JvmClassName {
        return JvmClassName(id.toLowerCase() + ".plugin." + classNameSuffix.capitalize())
    }
}

class CustomPluginProductionSpec(baseName: String, id: String) : PluginProductionSpec(baseName, id) {
}

class JavaConventionPluginProductionSpec(baseName: String, id: String) : PluginProductionSpec(baseName, id) {
}