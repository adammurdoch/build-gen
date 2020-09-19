package net.rubygrapefruit.gen.specs

interface PluginProductionSpec {
    val id: String
    val workerTaskName: String
    val lifecycleTaskName: String

    /**
     * Creates a unique identifier based on the identity of this plugin.
     */
    fun identifier(suffix: String): String

    /**
     * Creates a unique fully-qualified class name based on the identity of this plugin.
     */
    fun className(classNameSuffix: String): JvmClassName
}