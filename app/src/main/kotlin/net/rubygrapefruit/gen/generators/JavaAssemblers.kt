package net.rubygrapefruit.gen.generators

import net.rubygrapefruit.gen.files.JavaSourceFileBuilder
import net.rubygrapefruit.gen.files.JvmType
import net.rubygrapefruit.gen.specs.JavaLibraryApiSpec
import net.rubygrapefruit.gen.specs.ProjectSpec

fun addEntryPoint(spec: ProjectSpec, target: JavaSourceFileBuilder.Statements) {
    if (spec.usesLibraries.isEmpty()) {
        return
    }
    val setType = JvmType.type(Set::class, String::class)
    val linkedHashSetType = JvmType.type(LinkedHashSet::class, String::class)
    target.variableDefinition(setType, "seen", linkedHashSetType.newInstance())
    addReferences(spec, target);
    target.methodCall("System.out.println(\"libraries = \" + seen)")
}

fun addReferences(spec: ProjectSpec, target: JavaSourceFileBuilder.Statements) {
    target.ifStatement("seen.add(\"${spec.name}\")") {
        for (library in spec.usesLibraries) {
            if (library.api is JavaLibraryApiSpec) {
                methodCall("${library.api.methodReference.className.name}.${library.api.methodReference.methodName}(seen)")
            }
        }
    }
}