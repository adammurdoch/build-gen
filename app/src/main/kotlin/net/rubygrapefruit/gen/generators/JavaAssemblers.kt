package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.Expression
import net.rubygrapefruit.gen.files.JavaSourceFileBuilder
import net.rubygrapefruit.gen.files.JvmType
import net.rubygrapefruit.gen.files.LocalVariable
import net.rubygrapefruit.gen.specs.JavaLibraryApiSpec
import net.rubygrapefruit.gen.specs.ProjectSpec

fun JavaSourceFileBuilder.Statements.addEntryPoint(spec: ProjectSpec) {
    if (spec.usesLibraries.isEmpty()) {
        return
    }
    val setType = JvmType.type(Set::class, String::class)
    val linkedHashSetType = JvmType.type(LinkedHashSet::class, String::class)
    val seen = variableDefinition(setType, "seen", linkedHashSetType.newInstance())
    addReferences(spec, seen)
    log("libraries = ", seen)
}

fun JavaSourceFileBuilder.Statements.addReferences(spec: ProjectSpec, localVar: LocalVariable) {
    ifStatement(localVar.methodCall("add", Expression.string(spec.name))) {
        for (library in spec.usesLibraries) {
            if (library.api is JavaLibraryApiSpec) {
                staticMethodCall(JvmType.type(library.api.methodReference.className), library.api.methodReference.methodName, localVar)
            }
        }
    }
}