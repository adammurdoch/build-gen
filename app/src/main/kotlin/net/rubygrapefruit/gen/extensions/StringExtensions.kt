package net.rubygrapefruit.gen.extensions

fun String.capitalized() = replaceFirstChar { it.uppercaseChar() }
