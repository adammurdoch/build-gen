package net.rubygrapefruit.gen.specs

import net.rubygrapefruit.gen.builders.DefaultRootProjectBuilder
import net.rubygrapefruit.gen.builders.RootProjectBuilder
import java.nio.file.Path

class BuildSpec(
    val displayName: String,
    val rootDir: Path,
    val includeConfigurationCacheProblems: Boolean,
    private val components: BuildComponentsSpec,
    private val childBuilds: List<BuildSpec>,
    private val includeSelf: Boolean,
    private val emptyComponents: List<EmptyComponentProductionSpec>
) {
    val includedBuilds: List<BuildSpec>
        get() = if (includeSelf) {
            listOf(this) + childBuilds
        } else {
            childBuilds
        }

    val producesPlugins: List<PluginProductionSpec>
        get() {
            val plugins = mutableListOf<PluginProductionSpec>()
            components.visit { spec -> if (spec is PluginProductionSpec) plugins.add(spec) }
            return plugins
        }

    val producesLibraries: List<ExternalLibraryProductionSpec>
        get() {
            val libraries = mutableListOf<ExternalLibraryProductionSpec>()
            components.visit { spec -> if (spec is ExternalLibraryProductionSpec) libraries.add(spec) }
            return libraries
        }

    /**
     * Visits the components of this build. Visits the dependencies of a component before visiting the component
     */
    fun visit(visitor: BuildComponentVisitor) {
        val queue = mutableListOf<BuildComponentProductionSpec>()
        components.visit(queue::add)
        val seen = mutableSetOf<BuildComponentProductionSpec>()
        val visited = mutableSetOf<BuildComponentProductionSpec>()
        while (queue.isNotEmpty()) {
            val component = queue.first()
            if (seen.add(component)) {
                queue.addAll(component.usesLibrariesFromSameBuild)
                queue.addAll(component.usesImplementationLibraries)
            } else {
                queue.removeFirst()
                if (visited.add(component)) {
                    component.accept(visitor)
                }
            }
        }
        for (component in emptyComponents) {
            visitor.visitEmptyComponent(component)
        }
    }

    /**
     * Creates a project tree for this build.
     */
    fun projects(body: RootProjectBuilder.() -> Unit): RootProjectSpec {
        val builder = DefaultRootProjectBuilder(this)
        body(builder)
        return builder.build()
    }
}