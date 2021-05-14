package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.builders.DefaultBuildTreeBuilder
import net.rubygrapefruit.gen.files.*
import net.rubygrapefruit.gen.generators.*
import net.rubygrapefruit.gen.templates.*
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
    val treeStructure = prompter.select("Select build tree structure", BuildTreeStructure.values())
    val buildLogic = prompter.select("Select build logic structure", BuildLogic.values())
    val implementation = prompter.select("Select implementation", buildLogic.applicableImplementations)
    val theme = prompter.select("Select theme", Theme.values())
    val dsl = prompter.select("Select DSL language", DslLanguage.values())
    val treeTemplate = BuildTreeTemplate.templateFor(treeStructure, buildLogic)
    val rootDir = File("build/test").absoluteFile.toPath()
    generate(rootDir, treeTemplate, implementation, theme, dsl)
}

fun generate(rootDir: Path, layout: BuildTreeTemplate, implementation: Implementation, theme: Theme, dsl: DslLanguage) {
    val builder = DefaultBuildTreeBuilder(rootDir, implementation.pluginSpecFactory, implementation.librarySpecFactory)
    layout.run { builder.applyTo() }
    theme.applyTo(builder)
    val buildTree = builder.build()

    val synchronizer = GeneratedDirectoryContentsSynchronizer()
    synchronizer.sync(buildTree.rootDir) { fileContext ->
        val problemGenerator = ConfigurationCacheProblemGenerator()
        val textFileGenerator = TextFileGenerator(fileContext)
        val sourceFileGenerator = SourceFileGenerator(textFileGenerator)
        val scriptGenerator = ScriptGenerator(dsl, textFileGenerator)
        val customPluginImplementationGenerator = CustomPluginImplementationAssembler(
            sourceFileGenerator
        )
        val javaConventionPluginImplementationGenerator = JavaConventionPluginImplementationAssembler()
        val pluginImplementationGenerator = PluginImplementationGenerator(
            sourceFileGenerator,
            listOf(
                problemGenerator.pluginImplementation(),
                customPluginImplementationGenerator.pluginImplementation(),
                javaConventionPluginImplementationGenerator.pluginImplementation()
            )
        )
        val pluginProducerAssembler = PluginProducerProjectAssembler(pluginImplementationGenerator.pluginImplementation())
        val javaLibraryAssembler = JavaLibraryImplementationAssembler(sourceFileGenerator)
        val projectGenerator = ProjectContentsGenerator(
            scriptGenerator,
            fileContext,
            listOf(pluginProducerAssembler.projectContents(), javaLibraryAssembler.projectContents(), problemGenerator.projectContents())
        )
        val buildContentsGenerator = BuildContentsGenerator(
            scriptGenerator,
            fileContext,
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
    return if (values.size == 1) {
        values[0]
    } else {
        values.get(select(prompt, values.map { it.toString() }, 0))
    }
}

fun <T> Prompter.select(prompt: String, values: List<T>): T {
    return if (values.size == 1) {
        values[0]
    } else {
        values.get(select(prompt, values.map { it.toString() }, 0))
    }
}
