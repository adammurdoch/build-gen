package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.specs.ProjectSpec
import java.nio.file.Files

class ProjectGenerator {
    fun project(): Generator<ProjectSpec> = Generator.of {
        Files.createDirectories(projectDir)
    }
}