package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.*

enum class Implementation(
    val pluginSpecFactory: PluginSpecFactory,
    val librarySpecFactory: LibrarySpecFactory
) {
    None(NothingPluginSpecFactory(), NothingLibrarySpecFactory()),
    Custom(CustomPluginSpecFactory(), CustomLibrarySpecFactory()),
    Java(JavaConventionPluginSpecFactory(), JavaLibrarySpecFactory())
}