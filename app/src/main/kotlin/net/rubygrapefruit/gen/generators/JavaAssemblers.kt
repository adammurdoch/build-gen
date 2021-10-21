package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.JavaSourceFileBuilder
import net.rubygrapefruit.gen.files.JvmType
import net.rubygrapefruit.gen.files.LocalVariable
import net.rubygrapefruit.gen.specs.JavaLibraryApiSpec
import net.rubygrapefruit.gen.specs.ProjectSpec

fun addEntryPoint(spec: ProjectSpec, target: JavaSourceFileBuilder.Statements) {
    if (spec.usesLibraries.isEmpty()) {
        return
    }
    val setType = JvmType.type(Set::class, String::class)
    val linkedHashSetType = JvmType.type(LinkedHashSet::class, String::class)
    val seen = target.variableDefinition(setType, "seen", linkedHashSetType.newInstance())
    addReferences(spec, seen, target);
    target.methodCall("System.out.println(\"libraries = \" + ${seen.name})")
}

fun addReferences(spec: ProjectSpec, localVar: LocalVariable, target: JavaSourceFileBuilder.Statements) {
    target.ifStatement("seen.add(\"${spec.name}\")") {
        for (library in spec.usesLibraries) {
            if (library.api is JavaLibraryApiSpec) {
                methodCall("${library.api.methodReference.className.name}.${library.api.methodReference.methodName}(${localVar.name})")
            }
        }
    }
}