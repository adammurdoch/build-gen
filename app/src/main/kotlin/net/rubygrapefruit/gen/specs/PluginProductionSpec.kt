package net.rubygrapefruit.gen.specs

interface PluginProductionSpec {
    val id: String
    val pluginImplementationClass: JvmClassName
    val taskImplementationClass: JvmClassName
    val workerTaskName: String
    val lifecycleTaskName: String
}