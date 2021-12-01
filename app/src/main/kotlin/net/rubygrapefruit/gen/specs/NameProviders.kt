package net.rubygrapefruit.gen.specs

sealed interface NameProvider {
    fun next(): String

    fun push(names: List<String>): NameProvider = ChainedNames(names, this)
}

private class ChainedNames(
    private val names: List<String>,
    private val next: NameProvider
) : NameProvider {
    private var counter = 0

    override fun next(): String {
        counter++
        return if (counter <= names.size) {
            names[counter - 1]
        } else {
            next.next()
        }
    }
}

private class FixedNames(
    private val baseName: String
) : NameProvider {
    private var counter = 0

    override fun next(): String {
        counter++
        return if (counter == 1) {
            baseName
        } else {
            "$baseName${counter}"
        }
    }
}

class MutableNames(
    defaultNames: NameProvider
) : NameProvider {
    private var delegate: NameProvider = defaultNames

    fun replace(provider: NameProvider) {
        delegate = provider
    }

    override fun next(): String {
        return delegate.next()
    }
}

class Names {
    private val providers = mutableMapOf<String, FixedNames>()

    fun names(baseName: String): NameProvider = providers.getOrPut(baseName) { FixedNames(baseName) }
}
