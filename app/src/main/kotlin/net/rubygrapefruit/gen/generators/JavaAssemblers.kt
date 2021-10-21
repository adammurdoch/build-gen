package net.rubygrapefruit.gen.generators

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
    methodCall("System.out.println(\"libraries = \" + ${seen.name})")
}

fun JavaSourceFileBuilder.Statements.addReferences(spec: ProjectSpec, localVar: LocalVariable) {
    ifStatement("seen.add(\"${spec.name}\")") {
        for (library in spec.usesLibraries) {
            if (library.api is JavaLibraryApiSpec) {
                methodCall("${library.api.methodReference.className.name}.${library.api.methodReference.methodName}(${localVar.name})")
            }
        }
    }
}