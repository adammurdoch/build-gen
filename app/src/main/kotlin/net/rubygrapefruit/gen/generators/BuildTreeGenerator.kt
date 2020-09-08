package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.BuildTreeSpec
import java.nio.file.Files

class BuildTreeGenerator(private val buildGenerator: BuildGenerator) {
    fun generate(buildTree: BuildTreeSpec) {
        Files.createDirectories(buildTree.rootDir)
        for (build in buildTree.builds) {
            buildGenerator.generate(build)
        }
    }
}