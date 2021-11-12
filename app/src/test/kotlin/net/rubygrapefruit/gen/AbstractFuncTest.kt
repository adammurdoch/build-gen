package net.rubygrapefruit.gen

import junit.framework.TestCase.assertTrue
import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
import net.rubygrapefruit.gen.templates.*
import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.BeforeTest

abstract class AbstractFuncTest {
    val rootDir = File("build/test/gen")
    val testDir = TemporaryFolder(rootDir)

    @BeforeTest
    fun setup() {
        rootDir.mkdirs()
        testDir.create()
    }

    fun generate(
        productionTreeStructure: ProductionTreeStructure,
        buildLogic: BuildLogic,
        implementation: Implementation,
        templateOptions: List<TemplateOption> = emptyList(),
        dsl: DslLanguage = DslLanguage.GroovyDsl
    ): File {
        val dir = testDir.newFolder()
        generate(dir, productionTreeStructure, buildLogic, implementation, templateOptions, dsl)
        return dir
    }

    fun generate(
        dir: File,
        productionTreeStructure: ProductionTreeStructure,
        buildLogic: BuildLogic,
        implementation: Implementation,
        templateOptions: List<TemplateOption> = emptyList(),
        dsl: DslLanguage = DslLanguage.GroovyDsl
    ) {
        val withStructure = RootParameters().options.filter { it.productionStructure == productionTreeStructure }.flatMap { it.options }
        assertTrue(withStructure.isNotEmpty())
        val withBuildLogic = withStructure.filter { it.buildLogic == buildLogic }.flatMap { it.options }
        assertTrue(withBuildLogic.isNotEmpty())
        val withImplementation = withBuildLogic.filter { it.implementation == implementation }
        assertTrue(withImplementation.size == 1)
        var parameters = withImplementation.first()
        for (option in templateOptions) {
            parameters = parameters.enable(option)
        }

        generate(dir.toPath(), parameters, dsl, GeneratedDirectoryContentsSynchronizer())
    }

    fun runBuild(dir: File, vararg args: String) {
        val runner = GradleRunner.create()
        runner.withProjectDir(dir)
        runner.withArguments(args.toList())
        runner.forwardOutput()

        println("Running build ...")
        runner.build()
    }

    fun buildTree(dir: File, dsl: DslLanguage = DslLanguage.GroovyDsl): BuildTreeFixture {
        assertTrue(dir.isDirectory)
        assertTrue(GeneratedDirectoryContentsSynchronizer().isGenerated(dir.toPath()))
        val buildTree = BuildTreeFixture(dir, dsl)
        buildTree.assertHasRootBuild()
        return buildTree
    }

    fun application(dir: File, dsl: DslLanguage = DslLanguage.GroovyDsl): ApplicationFixture {
        val buildTree = buildTree(dir, dsl)
        buildTree.assertHasProject("app")
        buildTree.assertHasProject("util")
        return ApplicationFixture(buildTree)
    }
}