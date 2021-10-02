package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.HtmlGenerator
import net.rubygrapefruit.gen.specs.*

class ReportGenerator(
    val htmlGenerator: HtmlGenerator
) {
    fun treeContents(): Generator<BuildTreeSpec> = Generator.of {
        htmlGenerator.file(rootDir.resolve("report.html")) {
            println("<!DOCTYPE html>")
            println("<html>")
            println("<body>")
            println("<script src=\"https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js\"></script>")
            println("<div class=\"mermaid\">")
            println("flowchart LR")
            val ids = Ids()
            for (build in builds) {
                val id = ids.id(build)
                println("  subgraph $id [${build.displayName}]")
                for (plugin in build.producesPlugins) {
                    val pluginId = ids.id(plugin)
                    println("  $pluginId([plugin ${plugin.id}])")
                }
                for (app in build.producesApps) {
                    val appId = ids.id(app)
                    println("  $appId([app ${app.baseName.camelCase}])")
                }
                for (library in build.producesLibraries) {
                    val libId = ids.id(library)
                    println("  $libId([lib ${library.coordinates.group}:${library.coordinates.name}:${library.coordinates.version}])")
                }
                for (library in build.implementationLibraries) {
                    val libId = ids.id(library)
                    println("  $libId([internal lib ${library.baseName.camelCase}])")
                }
                println("  end")
            }
            for (build in builds) {
                for (plugin in build.usesPlugins) {
                    println("  ${ids.id(build)}-->${ids.id(findProducer(builds, plugin))}")
                }
                for (app in build.producesApps) {
                    for (plugin in app.usesPlugins) {
                        println("  ${ids.id(app)}-->${ids.id(findProducer(builds, plugin))}")
                    }
                    for (required in app.usesLibraries) {
                        println("  ${ids.id(app)}-->${ids.id(findProducer(builds, required))}")
                    }
                    for (required in app.usesImplementationLibraries) {
                        println("  ${ids.id(app)}-->${ids.id(required)}")
                    }
                }
                for (library in build.producesLibraries) {
                    for (required in library.usesLibraries) {
                        println("  ${ids.id(library)}-->${ids.id(findProducer(builds, required))}")
                    }
                    for (required in library.usesLibrariesFromSameBuild) {
                        println("  ${ids.id(library)}-->${ids.id(required)}")
                    }
                    for (required in library.usesImplementationLibraries) {
                        println("  ${ids.id(library)}-->${ids.id(required)}")
                    }
                }
            }
            println("</div>")
            println("</body>")
            println("</html>")
        }
    }

    private fun findProducer(builds: List<BuildSpec>, useSpec: PluginUseSpec): PluginProductionSpec {
        for (build in builds) {
            for (plugin in build.producesPlugins) {
                if (plugin.id == useSpec.id) {
                    return plugin
                }
            }
        }
        throw IllegalArgumentException()
    }

    private fun findProducer(builds: List<BuildSpec>, useSpec: ExternalLibraryUseSpec): ExternalLibraryProductionSpec {
        for (build in builds) {
            for (library in build.producesLibraries) {
                if (library.coordinates == useSpec.coordinates) {
                    return library
                }
            }
        }
        throw IllegalArgumentException()
    }

    private class Ids {
        private val items = mutableMapOf<Any, String>()

        fun id(item: Any): String {
            return items.getOrPut(item) { "id${items.size}" }
        }
    }
}