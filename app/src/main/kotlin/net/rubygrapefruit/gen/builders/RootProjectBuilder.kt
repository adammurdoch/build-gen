package net.rubygrapefruit.gen.builders

/**
 * A builder for the root project of a build.
 */
interface RootProjectBuilder {
    fun root(body: ProjectBuilder.() -> Unit)

    /**
     * Adds the given project, failing if already defined.
     */
    fun <T> project(name: String, body: ProjectBuilder.() -> T): T
}