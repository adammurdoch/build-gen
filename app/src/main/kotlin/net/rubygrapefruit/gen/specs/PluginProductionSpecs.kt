package net.rubygrapefruit.gen.specs

import net.rubygrapefruit.gen.extensions.capitalized

sealed class PluginProductionSpec(
    protected val baseName: BaseName,
    val id: String,
): BuildComponentProductionSpec(emptyList(), emptyList(), emptyList(), emptyList()) {
    abstract fun toUseSpec(): PluginUseSpec

    /**
     * Creates a unique identifier based on the identity of this plugin.
     */
    fun identifier(suffix: String): String = baseName.camelCase + suffix.capitalized()

    /**
     * Creates a unique fully-qualified class name based on the identity of this plugin.
     */
    fun className(classNameSuffix: String): JvmClassName {
        return JvmClassName(baseName.lowerCaseDotSeparator + ".plugin." + classNameSuffix.capitalized())
    }

    override fun accept(visitor: BuildComponentVisitor) {
        visitor.visitPlugin(this)
    }
}

class CustomPluginProductionSpec(baseName: BaseName, val artifactType: String, id: String) : PluginProductionSpec(baseName, id) {
    val lifecycleTaskName: String
        get() = baseName.camelCase

    val workerTaskName: String
        get() = identifier("worker")

    override fun toUseSpec() = PluginUseSpec(id, workerTaskName, false)
}

class JavaConventionPluginProductionSpec(baseName: BaseName, id: String) : PluginProductionSpec(baseName, id) {
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