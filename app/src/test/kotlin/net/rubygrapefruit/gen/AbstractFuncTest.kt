package net.rubygrapefruit.gen

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

    fun runBuild(dir: File, vararg args: String) {
        val runner = GradleRunner.create()
        runner.withProjectDir(dir)
        runner.withArguments(args.toList())
        runner.forwardOutput()

        println("Running build ...")
        runner.build()
    }
}