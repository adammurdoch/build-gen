package net.rubygrapefruit.gen.builders

/**
 * A mutable builder for the build tree structure.
 */
interface BuildTreeBuilder {
    var includeConfigurationCacheProblems: Boolean

    /**
     * Adds a child build.
     */
    fun <T> build(name: String, body: BuildBuilder.() -> T): T

    fun <T> mainBuild(body: BuildBuilder.() -> T): T
}