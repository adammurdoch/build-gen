package net.rubygrapefruit.gen.builders

/**
 * A mutable builder for the build tree structure.
 */
interface BuildTreeBuilder {
    var includeConfigurationCacheProblems: Boolean

    fun requireHeap(heapSize: String)

    val mainBuild: BuildBuilder

    fun <T> mainBuild(body: BuildBuilder.() -> T): T

    /**
     * Adds another build that is not part of the main build
     */
    fun build(name: String, body: BuildBuilder.() -> Unit)
}