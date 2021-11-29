package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.*

/**
 * Builds a set of zero or more internal libraries.
 */
class InternalLibrariesBuilder(
    private val projectNames: NameProvider,
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
            val top = FlatBuilder(projectNames, librarySpecFactory)
            val middle = InternalLibrariesBuilder(projectNames, librarySpecFactory)
            val bottom = InternalLibrariesBuilder(projectNames, librarySpecFactory)
            val perComponent = count / 3
            val remainder = count % 3

            top.add(perComponent + if (remainder > 0) 1 else 0)
            top.usesPlugins(plugins)
            top.usesLibraries(externalLibraries)
            top.usesLibraries(internalLibraries)
            top.usesLibraries(incomingLibraries)

            middle.add(perComponent + if (remainder > 1) 1 else 0)
            middle.usesPlugins(plugins)
            middle.usesLibraries(externalLibraries)
            middle.usesLibraries(internalLibraries)
            middle.usesLibraries(incomingLibraries)

            bottom.add(perComponent)
            bottom.usesPlugins(plugins)

            // TODO - distribute dependencies across top libraries

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