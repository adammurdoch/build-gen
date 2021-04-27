package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.builders.DefaultBuildTreeBuilder
import net.rubygrapefruit.gen.files.*
import net.rubygrapefruit.gen.generators.*
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
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
    val treeTemplate = prompter.select("Select build tree structure", BuildTreeTemplate.values())
    val implementation = prompter.select("Select implementation", treeTemplate.applicableImplementations)
    val theme = prompter.select("Select theme", Theme.values())
    val dsl = prompter.select("Select DSL language", DslLanguage.values())
    val rootDir = File("build/test").absoluteFile.toPath()
    generate(rootDir, treeTemplate, implementation, theme, dsl)
}

fun generate(rootDir: Path, layout: BuildTreeTemplate, implementation: Implementation, theme: Theme, dsl: DslLanguage) {
    val builder = DefaultBuildTreeBuilder(rootDir, implementation.pluginSpecFactory)
    layout.run { builder.applyTo() }
    theme.applyTo(builder)
    val buildTree = builder.build()

    val synchronizer = GeneratedDirectoryContentsSynchronizer()
    synchronizer.sync(buildTree.rootDir) { fileContext ->
        val problemGenerator = ConfigurationCacheProblemGenerator()
        val textFileGenerator = TextFileGenerator(fileContext)
        val sourceFileGenerator = SourceFileGenerator(textFileGenerator)
        val scriptGenerator = ScriptGenerator(dsl, textFileGenerator)
        val pluginImplementationGenerator = CustomPluginImplementationGenerator(
            sourceFileGenerator,
            listOf(problemGenerator.pluginImplementation())
        )
        val pluginProducerGenerator = CustomPluginProducerGenerator(pluginImplementationGenerator.pluginImplementation())
        val projectGenerator = ProjectContentsGenerator(
            scriptGenerator,
            listOf(pluginProducerGenerator.projectContents(), problemGenerator.projectContents())
        )
        val buildContentsGenerator = BuildContentsGenerator(
            scriptGenerator,
            listOf(problemGenerator.buildContents()),
            projectGenerator.projectContents()
        )
        val buildTreeGenerator = BuildTreeContentsGenerator(buildContentsGenerator.buildContents())
        ParallelGenerationContext().use {
            buildTreeGenerator.generate(buildTree, it)
        }
    }
}


fun <T> Prompter.select(prompt: String, values: Array<T>): T {
    return values.get(select(prompt, values.map { it.toString() }, 0))
}

fun <T> Prompter.select(prompt: String, values: List<T>): T {
    return values.get(select(prompt, values.map { it.toString() }, 0))
}
