package net.rubygrapefruit.gen

import net.rubygrapefruit.platform.Native
import net.rubygrapefruit.platform.prompts.Prompter
import net.rubygrapefruit.platform.terminal.Terminals


fun main(args: Array<String>) {
    val terminals = Native.get(Terminals::class.java)
    if (!terminals.isTerminalInput || !terminals.isTerminal(Terminals.Output.Stdout)) {
        println("Not connected to a terminal")
        System.exit(1)
    }
    val prompter = Prompter(terminals)
    val layout = prompter.select("Select build tree structure", BuildTreeTemplate.values())
    val builder = BuildTreeBuilder()
    layout.applyTo(builder)
    val buildTree = builder.build()

    println()
    for (build in buildTree.builds) {
        println("- generate ${build.displayName} into ${build.rootDir}")
    }
    println()
}


fun <T> Prompter.select(prompt: String, values: Array<T>): T {
    return values.get(select(prompt, values.map { it.toString() }, 0))
}

enum class BuildTreeTemplate(val display: String) {
    MainBuildOnly("single build") {
        override fun applyTo(builder: BuildTreeBuilder) {
        }
    },
    BuildSrc("build with buildSrc") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.addBuildSrc()
        }
    },
    BuildLogicChildBuild("build logic in child build") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.addBuildLogicBuild()
        }
    },
    BuildLogicChildBuildAndBuildSrc("build logic in buildSrc and child build") {
        override fun applyTo(builder: BuildTreeBuilder) {
            builder.addBuildSrc()
            builder.addBuildLogicBuild()
        }
    };

    override fun toString() = display

    abstract fun applyTo(builder: BuildTreeBuilder)
}