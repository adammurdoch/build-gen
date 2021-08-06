package net.rubygrapefruit.gen.builders

/**
 * Multiple libraries produced by a build, where the top library depends on the bottom library, possibly indirectly.
 */
interface LibrariesRef {
    val top: LibraryRef

    val bottom: LibraryRef
}