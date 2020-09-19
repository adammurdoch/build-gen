package net.rubygrapefruit.gen.files

import java.io.PrintWriter
import java.nio.file.Path

class ScriptGenerator(private val dsl: DslLanguage, private val textFileGenerator: TextFileGenerator) {
    fun settings(dir: Path): SettingsScriptBuilder {
        return SettingsScriptBuilderImpl(dir)
    }

    fun build(dir: Path): BuildScriptBuilder {
        return BuildScriptBuilderImpl(dir)
    }

    private interface BlockElement {
        fun PrintWriter.renderContents(prefix: String)
    }

    private class IncludedProjects : BlockElement {
        val names = mutableListOf<String>()

        override fun PrintWriter.renderContents(prefix: String) {
            if (names.isEmpty()) {
                return
            }
            println()
            for (name in names) {
                print(prefix)
                println("include(\"$name\")")
            }
        }
    }

    private class IncludedBuilds : BlockElement {
        val paths = mutableListOf<String>()

        override fun PrintWriter.renderContents(prefix: String) {
            if (paths.isEmpty()) {
                return
            }
            println()
            for (path in paths) {
                print(prefix)
                println("includeBuild(\"$path\")")
            }
        }
    }

    private class PropertyImpl(val name: String, val value: String) : BlockElement {
        override fun PrintWriter.renderContents(prefix: String) {
            print(prefix)
            print(name)
            print(" = \"")
            print(value)
            println("\"")
        }
    }

    private class MethodImpl(val text: String) : BlockElement {
        override fun PrintWriter.renderContents(prefix: String) {
            print(prefix)
            println(text)
        }
    }

    private open inner class HasBlockContents : ScriptBlockGenerator {
        val elements = mutableListOf<BlockElement>()

        override fun block(name: String, body: ScriptBlockGenerator.() -> Unit) {
            val block = NestedBlock(name)
            body(block)
            elements.add(block)
        }

        override fun namedItem(name: String, body: ScriptBlockGenerator.() -> Unit) {
            if (dsl == DslLanguage.KotlinDsl) {
                block("create(\"${name}\")", body)
            } else {
                block(name, body)
            }
        }

        override fun property(name: String, value: String) {
            elements.add(PropertyImpl(name, value))
        }

        override fun method(text: String) {
            elements.add(MethodImpl(text))
        }

        fun PrintWriter.renderElements(prefix: String) {
            for (element in elements) {
                element.run {
                    renderContents(prefix)
                }
            }
        }
    }

    private inner class NestedBlock(val name: String) : HasBlockContents(), BlockElement {
        override fun PrintWriter.renderContents(prefix: String) {
            print(prefix)
            print(name)
            println(" {")
            renderElements("$prefix    ")
            print(prefix)
            println("}")
        }
    }

    private inner class SettingsScriptBuilderImpl(val dir: Path) : HasBlockContents(), SettingsScriptBuilder {
        private val includedProjects = IncludedProjects()
        private val includedBuilds = IncludedBuilds()

        init {
            elements.add(includedProjects)
            elements.add(includedBuilds)
        }

        override fun includeProject(path: String) {
            includedProjects.names.add(path)
        }

        override fun includeBuild(path: String) {
            includedBuilds.paths.add(path)
        }

        override fun complete() {
            textFileGenerator.file(dir.resolve("settings.${dsl.extension}")) {
                renderElements("")
            }
        }
    }

    private class Dependencies : BlockElement {
        val implementation = mutableListOf<String>()

        override fun PrintWriter.renderContents(prefix: String) {
            if (implementation.isEmpty()) {
                return
            }
            println()
            println("dependencies {")
            for (project in implementation) {
                println("    implementation(project(\"${project}\"))")
            }
            println("}")
        }
    }

    private class Plugins : BlockElement {
        val ids = mutableListOf<String>()

        override fun PrintWriter.renderContents(prefix: String) {
            if (ids.isEmpty()) {
                return
            }
            println()
            println("plugins {")
            for (id in ids) {
                println("    id(\"$id\")")
            }
            println("}")
        }
    }

    private inner class BuildScriptBuilderImpl(val dir: Path) : HasBlockContents(), BuildScriptBuilder {
        private val plugins = Plugins()
        private val dependencies = Dependencies()

        init {
            elements.add(plugins)
            elements.add(dependencies)
        }

        override fun plugin(id: String) {
            plugins.ids.add(id)
        }

        override fun implementationDependency(projectPath: String) {
            dependencies.implementation.add(projectPath)
        }

        override fun complete() {
            textFileGenerator.file(dir.resolve("build.${dsl.extension}")) {
                renderElements("")
            }
        }
    }
}