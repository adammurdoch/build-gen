package net.rubygrapefruit.gen.specs

interface PluginUseSpec {
    val id: String

    /**
     * The name of a task added by the plugin.
     */
    val workerTaskName: String
}