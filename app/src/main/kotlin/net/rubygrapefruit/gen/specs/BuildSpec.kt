package net.rubygrapefruit.gen.specs

import net.rubygrapefruit.gen.builders.DefaultRootProjectBuilder
import net.rubygrapefruit.gen.builders.RootProjectBuilder
import java.nio.file.Path

class BuildSpec(
    val displayName: String,
    val rootDir: Path,
    val includeConfigurationCacheProblems: Boolean,
    val producesPlugins: List<PluginProductionSpec>,
    val producesLibraries: List<ExternalLibraryProductionSpec>,
    val producesApps: List<AppProductionSpec>,
    val producesInternalLibraries: List<InternalLibraryProductionSpec>,
    private val childBuilds: List<BuildSpec>,
    private val includeSelf: Boolean
) {
    val includedBuilds: List<BuildSpec>
        get() = if (includeSelf) {
            listOf(this) + childBuilds
        } else {
            childBuilds
        }

    /**
     * Visits the components of this build. Visits the dependencies of a component before visiting the component
     */
    fun visit(visitor: BuildComponentVisitor) {
        val queue = mutableListOf<BuildComponentProductionSpec>()
        queue.addAll(producesInternalLibraries)
        queue.addAll(producesLibraries)
        queue.addAll(producesApps)
        queue.addAll(producesPlugins)
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