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
        hasBuildFile(projectDir)
    }

    fun assertHasRootBuild() {
        hasSettingFile(rootDir)
        hasBuildFile(rootDir)
    }

    fun assertHasBuildSrc() {
        assertTrue(rootDir.resolve("buildSrc").isDirectory)
    }

    fun assertNoBuildSrc() {
        assertFalse(rootDir.resolve("buildSrc").exists())
    }

    fun assertHasBuild(name: String) {
        val dir = rootDir.resolve(name)
        hasSettingFile(dir)
        hasBuildFile(dir)
    }

    private fun hasBuildFile(dir: File) {
        when (dsl) {
            DslLanguage.GroovyDsl -> {
                assertTrue(dir.resolve("build.gradle").isFile)
                assertFalse(dir.resolve("build.gradle.kts").exists())
            }
            DslLanguage.KotlinDsl -> {
                assertFalse(dir.resolve("build.gradle").exists())
                assertTrue(dir.resolve("build.gradle.kts").isFile)
            }
        }
    }

    private fun hasSettingFile(dir: File) {
        when (dsl) {
            DslLanguage.GroovyDsl -> {
                assertTrue(dir.resolve("settings.gradle").isFile)
                assertFalse(dir.resolve("settings.gradle.kts").exists())
            }
            DslLanguage.KotlinDsl -> {
                assertFalse(dir.resolve("settings.gradle").exists())
                assertTrue(dir.resolve("settings.gradle.kts").isFile)
            }
        }
    }
}