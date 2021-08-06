package net.rubygrapefruit.gen.builders

/**
 * A builder for the root project of a build.
 */
interface RootProjectBuilder {
    fun root(body: ProjectBuilder.() -> Unit)

    /**
     * Add or configure the given project.
     */
    fun <T> project(name: String, body: ProjectBuilder.() -> T): T
}