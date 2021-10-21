package net.rubygrapefruit.gen.specs

import java.nio.file.Path

sealed class AppImplementationSpec

class CustomAppImplementationSpec : AppImplementationSpec()

class JavaAppImplementationSpec(
    val mainClassName: JvmClassName
) : AppImplementationSpec()

class ToolingApiClientSpec(
    val targetRootDir: Path
) : AppImplementationSpec()
