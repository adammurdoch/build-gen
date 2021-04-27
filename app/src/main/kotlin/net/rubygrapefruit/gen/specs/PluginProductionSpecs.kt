package net.rubygrapefruit.gen.specs

sealed class PluginProductionSpec(
    protected val baseName: String,
    val id: String,
) {
    abstract fun toUseSpec(): PluginUseSpec

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

    val workerTaskName: String
        get() = identifier("worker")

    override fun toUseSpec() = PluginUseSpec(id, workerTaskName, false)
}

class JavaConventionPluginProductionSpec(baseName: String, id: String) : PluginProductionSpec(baseName, id) {
    override fun toUseSpec() = PluginUseSpec(id, "compileJava", true)
}

class PluginUseSpec(
    val id: String,
    /**
     * The name of a task added by the plugin.
     */
    val workerTaskName: String,

    val canProduceJavaLibrary: Boolean
)