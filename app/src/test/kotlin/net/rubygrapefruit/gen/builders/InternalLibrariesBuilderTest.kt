package net.rubygrapefruit.gen.builders

import net.rubygrapefruit.gen.specs.InternalLibraryProductionSpec
import net.rubygrapefruit.gen.specs.Names
import net.rubygrapefruit.gen.specs.TypedNameProvider
import net.rubygrapefruit.gen.templates.Implementation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InternalLibrariesBuilderTest {
    val names = Names()
    val builder = InternalLibrariesBuilder(TypedNameProvider.of(names.names("top"), names.names("lib"), names.names("bottom")), Implementation.Java.librarySpecFactory)

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
        assertEquals(listOf("bottom"), builder.contents.map { it.baseName.camelCase })
        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(1, builder.leaves.size)
    }

    @Test
    fun canContainTwo() {
        builder.add(2)
        assertEquals(2, builder.currentSize)

        assertEquals(2, builder.contents.size)
        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(listOf("top"), builder.exportedLibraries.libraries.map { it.baseName.camelCase })
        assertEquals(1, builder.leaves.size)
        assertEquals(listOf("bottom"), builder.leaves.map { it.baseName.camelCase })
    }

    @Test
    fun canContainThree() {
        builder.add(3)
        assertEquals(3, builder.currentSize)

        assertEquals(3, builder.contents.size)
        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(listOf("top"), builder.exportedLibraries.libraries.map { it.baseName.camelCase })
        assertEquals(2, builder.leaves.size) // internal dependency is only used by top
        assertEquals(listOf("lib", "bottom"), builder.leaves.map { it.baseName.camelCase })
    }

    @Test
    fun canContainFour() {
        builder.add(4)
        assertEquals(4, builder.currentSize)

        assertEquals(4, builder.contents.size)
        assertEquals(1, builder.exportedLibraries.libraries.size)
        assertEquals(listOf("top"), builder.exportedLibraries.libraries.map { it.baseName.camelCase })
        assertEquals(2, builder.leaves.size)
        assertEquals(listOf("lib2", "bottom"), builder.leaves.map { it.baseName.camelCase })
    }

    @Test
    fun canContainNine() {
        builder.add(9)
        assertEquals(9, builder.currentSize)

        assertEquals(9, builder.contents.size)
        assertEquals(2, builder.exportedLibraries.libraries.size)
        assertEquals(listOf("top", "top2"), builder.exportedLibraries.libraries.map { it.baseName.camelCase })
        assertEquals(4, builder.leaves.size)
        assertEquals(listOf("lib5", "lib4", "lib2", "bottom"), builder.leaves.map { it.baseName.camelCase })
    }

    val InternalLibrariesBuilder.leaves: List<InternalLibraryProductionSpec> get() = contents.filter { it.usesImplementationLibraries.isEmpty() }
}