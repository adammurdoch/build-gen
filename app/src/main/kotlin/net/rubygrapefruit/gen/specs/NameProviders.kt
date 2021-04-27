package net.rubygrapefruit.gen.specs

interface NameProvider {
    fun next(): String
}

class FixedNames(
    private val names: List<String>,
    private val defaultName: String
) : NameProvider {
    private var counter = 0

    override fun next(): String {
        counter++
        return if (counter <= names.size) {
            names[counter - 1]
        } else {
            "$defaultName${counter - names.size}"
        }
    }
}