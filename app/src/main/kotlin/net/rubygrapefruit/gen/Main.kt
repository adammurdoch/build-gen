package net.rubygrapefruit.gen

import net.rubygrapefruit.platform.Native
import net.rubygrapefruit.platform.prompts.Prompter
import net.rubygrapefruit.platform.terminal.Terminals
import java.io.File


fun main(args: Array<String>) {
    val terminals = Native.get(Terminals::class.java)
    if (!terminals.isTerminalInput || !terminals.isTerminal(Terminals.Output.Stdout)) {
        println("Not connected to a terminal")
        System.exit(1)
    }
    val prompter = Prompter(terminals)
    val layout = prompter.select("Select build tree structure", BuildTreeTemplate.values())
    val builder = BuildTreeBuilder(File("build/test").absoluteFile.toPath())
    layout.applyTo(builder)
    val buildTree = builder.build()

    println()
    for (build in buildTree.builds) {
        println("- generate ${build.displayName}")
        println("  - root dir: ${build.rootDir}")
        for (plugin in build.requiresPlugins) {
            println("  - uses plugin ${plugin.id} from ${plugin.producedBy.displayName}")
        }
        for (plugin in build.producesPlugins) {
            println("  - produces plugin ${plugin.id}")
        }
    }
    println()

    val generator = BuildTreeGenerator(BuildGenerator())
    generator.generate(buildTree)
}


fun <T> Prompter.select(prompt: String, values: Array<T>): T {
    return values.get(select(prompt, values.map { it.toString() }, 0))
}
