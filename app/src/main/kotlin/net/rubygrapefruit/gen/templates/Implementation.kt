package net.rubygrapefruit.gen.templates

import net.rubygrapefruit.gen.builders.*

enum class Implementation(
    val pluginSpecFactory: PluginSpecFactory,
    val librarySpecFactory: LibrarySpecFactory,
    val applicationSpecFactory: ApplicationSpecFactory
) {
    None(NothingPluginSpecFactory(), NothingLibrarySpecFactory(), NothingApplicationSpecFactory()),
    Custom(CustomPluginSpecFactory(), CustomLibrarySpecFactory(), CustomApplicationSpecFactory()),
    Java(JavaConventionPluginSpecFactory(), JavaLibrarySpecFactory(), JavaApplicationSpecFactory())
}