package net.rubygrapefruit.gen

import junit.framework.TestCase.assertTrue
import net.rubygrapefruit.gen.files.DslLanguage
import java.io.File
import kotlin.test.assertFalse

class BuildTreeFixture(
    val rootDir: File,
    val dsl: DslLanguage
) {
    fun assertHasProject(project: String) {
        val projectDir = rootDir.resolve(project)
        assertTrue(projectDir.isDirectory)
        assertTrue(buildFile(projectDir).isFile)
    }

    fun assertHasRootBuild() {
        assertTrue(settings(rootDir).isFile)
        assertTrue(buildFile(rootDir).isFile)
    }

    fun assertHasBuildSrc() {
        assertTrue(rootDir.resolve("buildSrc").isDirectory)
    }

    fun assertNoBuildSrc() {
        assertFalse(rootDir.resolve("buildSrc").exists())
    }

    fun assertHasBuild(name: String) {
        val dir = rootDir.resolve(name)
        assertTrue(settings(dir).isFile)
        assertTrue(buildFile(dir).isFile)
    }

    private fun settings(dir: File): File {
        return when (dsl) {
            DslLanguage.GroovyDsl -> dir.resolve("settings.gradle")
            DslLanguage.KotlinDsl -> dir.resolve("settings.gradle.kts")
        }
    }

    private fun buildFile(dir: File): File {
        return when (dsl) {
            DslLanguage.GroovyDsl -> dir.resolve("build.gradle")
            DslLanguage.KotlinDsl -> dir.resolve("build.gradle.kts")
        }
    }
}