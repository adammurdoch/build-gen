package net.rubygrapefruit.gen

import junit.framework.TestCase.assertTrue
import net.rubygrapefruit.gen.files.GeneratedDirectoryContentsSynchronizer
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

    fun buildTree(dir: File): BuildTreeFixture {
        assertTrue(dir.isDirectory)
        assertTrue(dir.resolve("settings.gradle").isFile || dir.resolve("settings.gradle.kts").isFile)
        assertTrue(GeneratedDirectoryContentsSynchronizer().isGenerated(dir.toPath()))
        return BuildTreeFixture(dir)
    }

    fun application(dir: File): ApplicationFixture {
        buildTree(dir)
        val appDir = dir.resolve("app")
        assertTrue(appDir.exists())
        return ApplicationFixture()
    }

}