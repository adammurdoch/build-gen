package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*
import java.lang.Integer.max
import kotlin.math.min

/**
 * Builds a set of zero or more internal libraries.
 */
class InternalLibrariesBuilder(
    private val projectNames: TypedNameProvider,
    private val librarySpecFactory: LibrarySpecFactory,
) : ZeroOrMoreBuildComponentsBuilder<InternalLibraryProductionSpec>() {
    private lateinit var exported: List<InternalLibraryProductionSpec>

    val exportedLibraries: InternalLibrariesSpec = object : InternalLibrariesSpec {
        override val libraries: List<InternalLibraryProductionSpec>
            get() {
                contents
                return exported
            }
    }

    override fun createComponents(
        count: Int,
        plugins: PluginsSpec,
        externalLibraries: ExternalLibrariesSpec,
        internalLibraries: InternalLibrariesSpec,
        incomingLibraries: IncomingLibrariesSpec
    ): List<InternalLibraryProductionSpec> {
        if (count == 0) {
            exported = emptyList()
            return emptyList()
        } else {
            // top is 1/4 of total items, rounding up, up to 10
            val topSize = min(10, max(1, count / 4))

            // middle is 1/2 of remaining items, rounding down
            val middleSize = (count - topSize) / 2
            val bottomSize = count - topSize - middleSize

            // If middle and bottom are empty, use "bottom" names
            val topNames = if (topSize == count) projectNames.bottom.names else projectNames.top.names

            val top = FlatBuilder(topNames, librarySpecFactory)
            val middle = InternalLibrariesBuilder(projectNames.middle, librarySpecFactory)
            val bottom = InternalLibrariesBuilder(projectNames.bottom, librarySpecFactory)

            top.add(topSize)
            top.usesPlugins(plugins)
            top.usesLibraries(externalLibraries)
            top.usesLibraries(internalLibraries)
            top.usesLibraries(incomingLibraries)

            middle.add(middleSize)
            middle.usesPlugins(plugins)
            middle.usesLibraries(externalLibraries)
            middle.usesLibraries(internalLibraries)
            middle.usesLibraries(incomingLibraries)

            bottom.add(bottomSize)
            bottom.usesPlugins(plugins)

            // TODO - distribute incoming dependencies across top and middle libraries

            val bottomExported = PartitioningSpec(bottom.exportedLibraries)
            top.usesLibraries(middle.exportedLibraries)
            top.usesLibraries(bottomExported.left)
            middle.usesLibraries(bottomExported.right)

            exported = top.contents

            return top.contents + middle.contents + bottom.contents
        }
    }

    private class PartitioningSpec(val from: InternalLibrariesSpec) {
        val left: InternalLibrariesSpec = object : InternalLibrariesSpec {
            override val libraries: List<InternalLibraryProductionSpec>
                get() {
                    val allLibraries = from.libraries
                    val count = allLibraries.size / 2 + allLibraries.size % 2
                    return allLibraries.take(count)
                }
        }

        val right: InternalLibrariesSpec = object : InternalLibrariesSpec {
            override val libraries: List<InternalLibraryProductionSpec>
                get() {
                    val allLibraries = from.libraries
                    val count = allLibraries.size / 2 + allLibraries.size % 2
                    return allLibraries.drop(count)
                }
        }
    }

    private class FlatBuilder(
        private val projectNames: NameProvider,
        private val librarySpecFactory: LibrarySpecFactory,
    ) : ZeroOrMoreBuildComponentsBuilder<InternalLibraryProductionSpec>() {
        override fun createComponents(count: Int, plugins: PluginsSpec, externalLibraries: ExternalLibrariesSpec, internalLibraries: InternalLibrariesSpec, incomingLibraries: IncomingLibrariesSpec): List<InternalLibraryProductionSpec> {
            val results = mutableListOf<InternalLibraryProductionSpec>()
            for (i in 0 until count) {
                results.add(nextLibrary(plugins.plugins, externalLibraries.libraries, internalLibraries.libraries, incomingLibraries.libraries))
            }
            return results
        }

        private fun nextLibrary(
            plugins: List<PluginUseSpec>,
            externalLibraries: List<ExternalLibraryProductionSpec>,
            internalLibraries: List<InternalLibraryProductionSpec>,
            incomingLibraries: List<ExternalLibraryUseSpec>
        ): InternalLibraryProductionSpec {
            val libraryName = BaseName(projectNames.next())
            val spec = librarySpecFactory.library(libraryName)
            return InternalLibraryProductionSpec(libraryName, spec, plugins, incomingLibraries, externalLibraries, internalLibraries)
        }
    }
}