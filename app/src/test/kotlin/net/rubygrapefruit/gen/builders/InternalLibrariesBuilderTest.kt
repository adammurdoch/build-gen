package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.FixedNames
import net.rubygrapefruit.gen.specs.InternalLibraryProductionSpec
import net.rubygrapefruit.gen.templates.Implementation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InternalLibrariesBuilderTest {
    val builder = InternalLibrariesBuilder(FixedNames("lib"), Implementation.Java.librarySpecFactory)

    @Test
    fun canBeEmpty() {
        assertEquals(0, builder.currentSize)

        assertTrue(builder.exportedLibraries.libraries.isEmpty())
        assertTrue(builder.contents.isEmpty())
        assertTrue(builder.leaves.isEmpty())
    }

    @Test
    fun canContainOne() {
        builder.add(1)
        assertEquals(1, builder.currentSize)

        assertEquals(1, builder.contents.size)
        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(1, builder.leaves.size)
    }

    @Test
    fun canContainTwo() {
        builder.add(2)
        assertEquals(2, builder.currentSize)

        assertEquals(2, builder.contents.size)
        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(1, builder.leaves.size)
    }

    @Test
    fun canContainThree() {
        builder.add(3)
        assertEquals(3, builder.currentSize)

        assertEquals(3, builder.contents.size)
        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(2, builder.leaves.size) // internal dependency is only used by top
    }

    @Test
    fun canContainFour() {
        builder.add(4)
        assertEquals(4, builder.currentSize)

        assertEquals(4, builder.contents.size)
        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(2, builder.leaves.size)
    }

    @Test
    fun canContainNine() {
        builder.add(9)
        assertEquals(9, builder.currentSize)

        assertEquals(9, builder.contents.size)
        assertEquals(2, builder.exportedLibraries.libraries.size)
        assertEquals(4, builder.leaves.size)
    }

    val InternalLibrariesBuilder.leaves: List<InternalLibraryProductionSpec> get() = contents.filter { it.usesImplementationLibraries.isEmpty() }
}