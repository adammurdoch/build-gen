package net.rubygrapefruit.gen.specs

class BaseName private constructor(
    private val parts: List<String>
) {
    constructor(path: String) : this(path.split('.'))

    operator fun plus(path: String) = BaseName(parts + listOf(path))

    val lowerCaseDotSeparator: String
        get() = parts.joinToString(".") { it.lowercase() }

    val capitalCase: String
        get() = parts.joinToString { s -> s.replaceFirstChar { c -> c.uppercaseChar() } }

    val camelCase: String
        get() = parts.first().replaceFirstChar { it.lowercaseChar() } + parts.drop(1).joinToString { s -> s.replaceFirstChar { c -> c.uppercaseChar() } }
}