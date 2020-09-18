package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.builders.BuildTreeBuilder
import net.rubygrapefruit.gen.files.*
import net.rubygrapefruit.gen.generators.*
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Theme
import net.rubygrapefruit.platform.Native
import net.rubygrapefruit.platform.prompts.Prompter
import net.rubygrapefruit.platform.terminal.Terminals
import java.io.File
import java.nio.file.Path


fun main(args: Array<String>) {
    val terminals = Native.get(Terminals::class.java)
    if (!terminals.isTerminalInput || !terminals.isTerminal(Terminals.Output.Stdout)) {
        println("Not connected to a terminal")
        System.exit(1)
    }
    val prompter = Prompter(terminals)
    val layout = prompter.select("Select build tree structure", BuildTreeTemplate.values())
    val theme = prompter.select("Select theme", Theme.values())
    val dsl = prompter.select("Select DSL language", DslLanguage.values())
    val rootDir = File("build/test").absoluteFile.toPath()
    generate(rootDir, layout, theme, dsl)
}

fun generate(rootDir: Path, layout: BuildTreeTemplate, theme: Theme, dsl: DslLanguage) {
    val builder = BuildTreeBuilder(rootDir)
    layout.applyTo(builder)
    theme.applyTo(builder)
    val buildTree = builder.build()

    println()
    println("* generating build with the following settings:")
    println("- DSL: ${dsl}")
    for (build in buildTree.builds) {
        println("- generate ${build.displayName}")
        println("  - root dir: ${build.rootDir}")
        for (plugin in build.usesPlugins) {
            println("  - uses plugin ${plugin.id} from ${plugin.producedBy.displayName}")
        }
        for (plugin in build.producesPlugins) {
            println("  - produces plugin ${plugin.id}")
        }
    }
    println()

    val synchronizer = GeneratedDirectoryContentsSynchronizer()
    synchronizer.sync(buildTree.rootDir) { fileContext ->
        val problemGenerator = ConfigurationCacheProblemGenerator()
        val textFileGenerator = TextFileGenerator(fileContext)
        val buildTreeGenerator = BuildTreeContentsGenerator(
                BuildContentsGenerator(
                        ScriptGenerator(dsl, textFileGenerator),
                        listOf(PluginProducerGenerator(SourceFileGenerator(textFileGenerator), listOf(problemGenerator.plugin())), problemGenerator.build())
                ))
        ParallelGenerationContext().use {
            buildTreeGenerator.generate(buildTree, it)
        }
    }
}


fun <T> Prompter.select(prompt: String, values: Array<T>): T {
    return values.get(select(prompt, values.map { it.toString() }, 0))
}
