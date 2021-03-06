package net.rubygrapefruit.gen

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import net.rubygrapefruit.gen.builders.DefaultBuildTreeBuilder
import net.rubygrapefruit.gen.files.*
import net.rubygrapefruit.gen.generators.*
import net.rubygrapefruit.gen.templates.*
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
    val regen by parser.option(ArgType.Boolean, fullName = "regen", description = "Re-generates a build").default(false)
    val generateAll by parser.option(ArgType.Boolean, fullName = "generate-all", description = "Generates all builds").default(false)
    parser.parse(args)

    if (regen && generateAll) {
        throw IllegalArgumentException("Cannot use --regen and --generate-all together")
    }

    val rootDir = File(dirName).canonicalFile.toPath()

    when {
        regen -> regenerate(rootDir)
        generateAll -> generateAll(rootDir)
        else -> promptAndGenerate(rootDir)
    }
}

fun generateAll(rootDir: Path) {
    for (productionParameters in RootParameters().options) {
        for (buildLogicParameters in productionParameters.options) {
            var permutationParameters = buildLogicParameters.options.find { it.implementation == Implementation.Java }
            if (permutationParameters == null) {
                permutationParameters = buildLogicParameters.options.first()
            }
            val permutationDir = rootDir.resolve(permutationParameters.treeTemplate.productionTreeStructure.name + "-" + permutationParameters.treeTemplate.buildLogic.name)
            val synchronizer = GeneratedDirectoryContentsSynchronizer(permutationDir)

            println("Generating into $permutationDir")

            checkCanGenerateInto(permutationDir, synchronizer)
            generate(permutationDir, permutationParameters, synchronizer)
        }
    }
}

fun regenerate(rootDir: Path) {
    val synchronizer = GeneratedDirectoryContentsSynchronizer(rootDir)
    checkCanGenerateInto(rootDir, synchronizer)
    if (!synchronizer.isGenerated()) {
        throw IllegalArgumentException("Target directory '$rootDir' does not contain a generated build.")
    }

    println("Regenerating $rootDir")
    val args = synchronizer.loadParameters()
    val parameters = Parameters.fromMap(args)
    generate(rootDir, parameters, synchronizer)
}

private fun promptAndGenerate(rootDir: Path) {
    val synchronizer = GeneratedDirectoryContentsSynchronizer(rootDir)
    checkCanGenerateInto(rootDir, synchronizer)

    println("Generating into $rootDir")

    val terminals = Native.get(Terminals::class.java)
    if (!terminals.isTerminalInput || !terminals.isTerminal(Terminals.Output.Stdout)) {
        println("Not connected to a terminal")
        System.exit(1)
    }
    val prompter = Prompter(terminals)
    val rootParameters = RootParameters()
    val structureParameters = prompter.select("Select production build tree structure", rootParameters.options)
    val buildLogicParameters = prompter.select("Select build logic structure", structureParameters.options)
    val implementationParameters = prompter.select("Select implementation", buildLogicParameters.options)
    val parameters = selectOptions(implementationParameters, prompter)
    generate(rootDir, parameters, synchronizer)
}

private fun checkCanGenerateInto(rootDir: Path, synchronizer: GeneratedDirectoryContentsSynchronizer) {
    if (rootDir.isRegularFile()) {
        throw IllegalArgumentException("Target directory '$rootDir' already exists and is a file.")
    }
    if (rootDir.isDirectory() && !rootDir.listDirectoryEntries().isEmpty() && !synchronizer.isGenerated()) {
        throw IllegalArgumentException("Target directory '$rootDir' is not empty and does not contain a generated build.")
    }
}

private fun selectOptions(parameters: Parameters, prompter: Prompter): Parameters {
    var current = parameters
    while (true) {
        val options = listOf(Finished) + current.availableOptions.map {
            when (it) {
                is BooleanParameter -> BooleanPrompt(it, current.enabled(it.templateOption))
                is EnumParameter<*> -> toPrompt(it, current)
            }
        }
        val selected = prompter.select("Select option", options)
        when (selected) {
            Finished -> return current
            is BooleanPrompt -> current = selected.parameter.apply(current, !selected.enabled)
            is EnumPrompt<*> -> current = selected.apply(current)
        }
    }
}

private fun <T : Enum<T>> toPrompt(parameter: EnumParameter<T>, parameters: Parameters) = EnumPrompt(parameter, parameter.value(parameters))

fun generate(
    rootDir: Path,
    parameters: Parameters,
    synchronizer: GeneratedDirectoryContentsSynchronizer
) {
    println("production structure: " + parameters.treeTemplate.productionTreeStructure.displayName)
    println("build logic structure: " + parameters.treeTemplate.buildLogic.displayName)
    println("implementation: " + parameters.implementation)
    println("dsl: " + parameters.dsl)
    for (option in parameters.enabledOptions) {
        println("${option.displayName}: enabled")
    }

    val builder = DefaultBuildTreeBuilder(rootDir, parameters.implementation)
    parameters.treeTemplate.applyTo(builder, parameters.enabledOptions)
    val buildTree = builder.build()

    synchronizer.sync(parameters) { fileContext ->
        val problemGenerator = ConfigurationCacheProblemGenerator()
        val textFileGenerator = TextFileGenerator(fileContext)
        val sourceFileGenerator = SourceFileGenerator(textFileGenerator)
        val scriptGenerator = ScriptGenerator(parameters.dsl, textFileGenerator)
        val customPluginImplementationGenerator = CustomPluginImplementationAssembler(
            sourceFileGenerator
        )
        val javaConventionPluginImplementationGenerator = JavaConventionPluginImplementationAssembler()
        val pluginImplementationGenerator = PluginImplementationGenerator(
            sourceFileGenerator,
            Assembler.of(
                problemGenerator.pluginImplementation(),
                customPluginImplementationGenerator.pluginImplementation(),
                javaConventionPluginImplementationGenerator.pluginImplementation()
            )
        )
        val pluginProducerAssembler = PluginProducerProjectAssembler(pluginImplementationGenerator.pluginImplementation())
        val javaLibraryAssembler = JavaLibraryImplementationAssembler(sourceFileGenerator)
        val javaApplicationAssembler = JavaAppImplementationAssembler(sourceFileGenerator)
        val toolingApiClientImplementationAssembler = ToolingApiClientImplementationAssembler(sourceFileGenerator)
        val projectGenerator = ProjectContentsGenerator(
            scriptGenerator,
            fileContext,
            Assembler.of(
                pluginProducerAssembler.projectContents(),
                javaLibraryAssembler.projectContents(),
                javaApplicationAssembler.projectContents(),
                toolingApiClientImplementationAssembler.projectContents(),
                problemGenerator.projectContents()
            )
        )
        val buildContentsGenerator = BuildContentsGenerator(
            scriptGenerator,
            fileContext,
            problemGenerator.buildContents(),
            projectGenerator.projectContents()
        )
        val reportGenerator = ReportGenerator(HtmlGenerator(textFileGenerator))
        val additionalFilesGenerator = BuildTreeAdditionalFilesGenerator(textFileGenerator)
        val buildTreeGenerator = BuildTreeContentsGenerator(
            buildContentsGenerator.buildContents(),
            Generator.of(
                reportGenerator.treeContents(),
                additionalFilesGenerator.treeContents()
            )
        )
        ParallelGenerationContext().use {
            buildTreeGenerator.generate(buildTree, it)
        }
    }
}

fun <T> Prompter.select(prompt: String, values: List<T>): T {
    return if (values.size == 1) {
        values[0]
    } else {
        values.get(select(prompt, values.map { it.toString() }, 0))
    }
}

private sealed class OptionPrompt

private class BooleanPrompt(val parameter: OptionalParameter<Boolean>, val enabled: Boolean) : OptionPrompt() {
    override fun toString(): String {
        return "$parameter - " + if (enabled) "enabled" else "disabled"
    }
}

private class EnumPrompt<T : Enum<T>>(val parameter: EnumParameter<T>, val value: T) : OptionPrompt() {
    override fun toString(): String {
        return "$parameter - $value"
    }

    fun apply(parameters: Parameters): Parameters {
        val candidates = parameter.candidates
        val newValue = if (value == candidates.last()) candidates.first() else candidates.get(candidates.indexOf(value) + 1)
        return parameter.apply(parameters, newValue)
    }
}

private object Finished : OptionPrompt() {
    override fun toString(): String {
        return "No further changes"
    }
}
