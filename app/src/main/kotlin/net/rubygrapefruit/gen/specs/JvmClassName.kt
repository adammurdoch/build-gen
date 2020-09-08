package net.rubygrapefruit.gen.specs

class JvmClassName(val name: String) {
    val packageName: String
        get() = name.substringBeforeLast(".")

    val simpleName: String
        get() = name.substringAfterLast(".")
}