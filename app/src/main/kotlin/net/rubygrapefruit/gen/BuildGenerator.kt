package net.rubygrapefruit.gen

import java.nio.file.Files

class BuildGenerator {
    fun generate(build: BuildSpec) {
        Files.createDirectories(build.rootDir)
        build.rootDir.resolve("settings.gradle").toFile().writeText("""
            // Generated file
            
        """.trimIndent())
    }
}