package net.rubygrapefruit.gen.specs

interface PluginUseSpec {
    val id: String
    val workerTaskName: String
    val producedBy: BuildSpec
}