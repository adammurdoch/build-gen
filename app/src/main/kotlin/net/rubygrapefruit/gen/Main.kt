package net.rubygrapefruit.gen

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import net.rubygrapefruit.gen.builders.DefaultBuildTreeBuilder
import net.rubygrapefruit.gen.files.*
import net.rubygrapefruit.gen.generators.*
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Implementation
import net.rubygrapefruit.gen.templates.ProductionBuildTreeStructure
import net.rubygrapefruit.gen.templates.Theme
import net.rubygrapefruit.platform.Native
import net.rubygrapefruit.platform.prompts.Prompter
import net.rubygrapefruit.platform.terminal.Terminals
import java.io.File
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries


fun main(args: Array<String>) {
    val parser = ArgParser("build-gen")
    val dirName by parser.option(ArgType.String, fullName = "dir", description = "Directory to generate build into").default(".")
    parser.parse(args)

    val rootDir = File(dirName).canonicalFile.toPath()
    val synchronizer = GeneratedDirectoryContentsSynchronizer()
    if (rootDir.isRegularFile()) {
        throw IllegalArgumentException("Target directory '$rootDir' already exists and is a file.")
    }
    if (rootDir.isDirectory() && !rootDir.listDirectoryEntries().isEmpty() && !synchronizer.isGenerated(rootDir)) {
        throw IllegalArgumentException("Target directory '$rootDir' is not empty and does not contain a generated build.")
    }
    println("Generating into $rootDir")

    val terminals = Native.get(Terminals::class.java)
    if (!terminals.isTerminalInput || !terminals.isTerminal(Terminals.Output.Stdout)) {
        println("Not connected to a terminal")
        System.exit(1)
    }
    val prompter = Prompter(terminals)
    val treeStructure = prompter.select("Select production build tree structure", ProductionBuildTreeStructure.values())
    val buildLogic = prompter.select("Select build logic structure", BuildTreeTemplate.buildLogicOptionsFor(treeStructure))
    val implementation = prompter.select("Select implementation", BuildTreeTemplate.implementationsFor(treeStructure, buildLogic))
    val theme = prompter.select("Select theme", Theme.values())
    val dsl = prompter.select("Select DSL language", DslLanguage.values())
    val treeTemplate = BuildTreeTemplate.templateFor(treeStructure, buildLogic)
    generate(rootDir, treeTemplate, implementation, theme, dsl, synchronizer)
}

fun generate(rootDir: Path, layout: BuildTreeTemplate, implementation: Implementation, theme: Theme, dsl: DslLanguage, synchronizer: GeneratedDirectoryContentsSynchronizer) {
    val builder = DefaultBuildTreeBuilder(rootDir, implementation.pluginSpecFactory, implementation.librarySpecFactory)
    layout.run { builder.applyTo() }
    theme.applyTo(builder)
    val buildTree = builder.build()

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
