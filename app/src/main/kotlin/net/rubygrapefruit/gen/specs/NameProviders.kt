package net.rubygrapefruit.gen.specs

sealed interface NameProvider {
    fun next(): String
}

class ChainedNames(
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

class FixedNames(
    private val defaultName: String
) : NameProvider {
    private var counter = 0

    override fun next(): String {
        counter++
        return if (counter == 1) {
            defaultName
        } else {
            "$defaultName${counter}"
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