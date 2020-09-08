package net.rubygrapefruit.gen.generators

enum class DslLanguage(private val displayName: String, val extension: String) {
    GroovyDsl("Groovy DSL", "gradle"),
    KotlinDsl("Kotlin DSL", "gradle.kts");

    override fun toString() = displayName
}