package net.rubygrapefruit.gen.specs

class BaseName(path: String) {
    private val parts: List<String> = path.split('.').map { it.toLowerCase() }

    operator fun plus(path: String) = BaseName("$lowerCaseDotSeparator.$path")

    val lowerCaseDotSeparator: String = parts.joinToString(".")

    val capitalCase: String = parts.joinToString { it.capitalize() }

    val camelCase: String = parts.first() + parts.drop(1).joinToString { it.capitalize() }
}