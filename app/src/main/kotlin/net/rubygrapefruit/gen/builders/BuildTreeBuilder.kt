package net.rubygrapefruit.gen.builders

/**
 * A mutable builder for the build tree structure.
 */
interface BuildTreeBuilder {
    var includeConfigurationCacheProblems: Boolean

    val mainBuild: BuildBuilder

    fun <T> mainBuild(body: BuildBuilder.() -> T): T
}