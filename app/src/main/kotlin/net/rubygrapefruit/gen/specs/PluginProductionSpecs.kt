package net.rubygrapefruit.gen.specs

sealed class PluginProductionSpec(
    protected val baseName: String,
    val id: String,
) {
    abstract val workerTaskName: String

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
    val lifecycleTaskName: String
        get() = baseName

    override val workerTaskName: String
        get() = identifier("worker")
}

class JavaConventionPluginProductionSpec(baseName: String, id: String) : PluginProductionSpec(baseName, id) {
    override val workerTaskName: String
        get() = "compileJava"
}