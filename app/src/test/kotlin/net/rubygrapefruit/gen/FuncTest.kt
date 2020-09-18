package net.rubygrapefruit.gen

import net.rubygrapefruit.gen.files.DslLanguage
import net.rubygrapefruit.gen.templates.BuildTreeTemplate
import net.rubygrapefruit.gen.templates.Theme
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test

class FuncTest {
    val rootDir = File("build/test/gen")
    val testDir = TemporaryFolder(rootDir)

    @BeforeTest
    fun setup() {
        rootDir.mkdirs()
        testDir.create()
    }

    @Test
    fun canGenerateWithGroovyDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuildAndBuildSrc, Theme.None, DslLanguage.GroovyDsl)
    }

    @Test
    fun canGenerateWithKotlinDsl() {
        val dir = testDir.newFolder()
        generate(dir.toPath(), BuildTreeTemplate.BuildLogicChildBuildAndBuildSrc, Theme.None, DslLanguage.KotlinDsl)
    }
}