package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.FixedNames
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
    }

    @Test
    fun canContainOne() {
        builder.add()
        assertEquals(1, builder.currentSize)

        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(1, builder.contents.size)
    }

    @Test
    fun canContainMultiple() {
        builder.add()
        builder.add()
        builder.add()
        assertEquals(3, builder.currentSize)

        assertEquals(3, builder.exportedLibraries.libraries.size)
        assertEquals(3, builder.contents.size)
    }
}