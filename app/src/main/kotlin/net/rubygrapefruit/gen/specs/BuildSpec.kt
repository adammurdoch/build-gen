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
    val implementationLibraries: List<InternalLibrarySpec>,
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
     * Creates a project tree for this build.
     */
    fun projects(body: RootProjectBuilder.() -> Unit): RootProjectSpec {
        val builder = DefaultRootProjectBuilder(this)
        body(builder)
        return builder.build()
    }
}