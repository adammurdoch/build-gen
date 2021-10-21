package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.HtmlGenerator
import net.rubygrapefruit.gen.specs.*
import java.io.PrintWriter

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
                build.visit(object : BuildComponentVisitor {
                    override fun visitPlugin(plugin: PluginProductionSpec) {
                        val pluginId = ids.id(plugin)
                        println("  $pluginId([plugin ${plugin.id}])")
                    }

                    override fun visitApp(app: AppProductionSpec) {
                        val appId = ids.id(app)
                        println("  $appId([app ${app.baseName.camelCase}])")
                    }

                    override fun visitLibrary(library: ExternalLibraryProductionSpec) {
                        val libId = ids.id(library)
                        println("  $libId([lib ${library.coordinates.group}:${library.coordinates.name}:${library.coordinates.version}])")
                    }

                    override fun visitInternalLibrary(library: InternalLibraryProductionSpec) {
                        val libId = ids.id(library)
                        println("  $libId([internal lib ${library.baseName.camelCase}])")
                    }

                    override fun visitEmptyComponent(component: EmptyComponentProductionSpec) {
                        val componentId = ids.id(component)
                        println("  $componentId([empty ${component.baseName.camelCase}])")
                    }
                })
                println("  end")
            }
            for (build in builds) {
                build.visit(object : BuildComponentVisitor {
                    override fun visitPlugin(plugin: PluginProductionSpec) {
                        edgesForComponent(plugin, ids, this@ReportGenerator, builds)
                    }

                    override fun visitApp(app: AppProductionSpec) {
                        edgesForComponent(app, ids, this@ReportGenerator, builds)
                    }

                    override fun visitLibrary(library: ExternalLibraryProductionSpec) {
                        edgesForComponent(library, ids, this@ReportGenerator, builds)
                    }

                    override fun visitInternalLibrary(library: InternalLibraryProductionSpec) {
                        edgesForComponent(library, ids, this@ReportGenerator, builds)
                    }
                })
            }
            println("</div>")
            println("</body>")
            println("</html>")
        }
    }

    private fun PrintWriter.edgesForComponent(
        component: BuildComponentProductionSpec,
        ids: Ids,
        reportGenerator: ReportGenerator,
        builds: List<BuildSpec>
    ) {
        val pluginsById = builds.flatMap { it.producesPlugins }.associateBy { it.id }
        val librariesByCoords = builds.flatMap { it.producesLibraries }.associateBy { it.coordinates }
        for (plugin in component.usesPlugins) {
            println("  ${ids.id(component)}-->${ids.id(reportGenerator.findProducer(pluginsById, plugin))}")
        }
        for (required in component.usesLibraries) {
            println("  ${ids.id(component)}-->${ids.id(reportGenerator.findProducer(librariesByCoords, required))}")
        }
        for (required in component.usesLibrariesFromSameBuild) {
            println("  ${ids.id(component)}-->${ids.id(required)}")
        }
        for (required in component.usesImplementationLibraries) {
            println("  ${ids.id(component)}-->${ids.id(required)}")
        }
    }

    private fun findProducer(pluginsById: Map<String, PluginProductionSpec>, useSpec: PluginUseSpec): PluginProductionSpec {
        return pluginsById.getValue(useSpec.id)
    }

    private fun findProducer(librariesByCoords: Map<ExternalLibraryCoordinates, ExternalLibraryProductionSpec>, useSpec: ExternalLibraryUseSpec): ExternalLibraryProductionSpec {
        return librariesByCoords.getValue(useSpec.coordinates)
    }

    private class Ids {
        private val items = mutableMapOf<Any, String>()

        fun id(item: Any): String {
            return items.getOrPut(item) { "id${items.size}" }
        }
    }
}