package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.LibraryUseSpec

/**
 * A builder for the root project of a build.
 */
interface RootProjectBuilder {
    fun root(body: ProjectBuilder.() -> Unit)

    fun project(name: String, body: ProjectBuilder.() -> Unit): LibraryUseSpec
}