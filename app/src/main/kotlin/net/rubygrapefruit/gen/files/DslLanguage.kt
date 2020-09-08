package net.rubygrapefruit.gen.files

enum class DslLanguage(private val displayName: String, val extension: String) {
    GroovyDsl("Groovy DSL", "gradle"),
    KotlinDsl("Kotlin DSL", "gradle.kts");

    override fun toString() = displayName
}