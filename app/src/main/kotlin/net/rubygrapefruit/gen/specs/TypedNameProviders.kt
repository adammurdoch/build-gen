package net.rubygrapefruit.gen.specs

sealed interface TypedNameProvider {
    val names: NameProvider

    val top: TypedNameProvider

    val middle: TypedNameProvider

    val bottom: TypedNameProvider

    companion object {
        fun of(topNames: NameProvider, middleNames: NameProvider, bottomNames: NameProvider): TypedNameProvider {
            return DefaultTypedNameProvider(middleNames, topNames, middleNames, bottomNames)
        }
    }
}

private class DefaultTypedNameProvider(
    override val names: NameProvider,
    private val topNames: NameProvider,
    private val middleNames: NameProvider,
    private val bottomNames: NameProvider
) : TypedNameProvider {
    override val top: TypedNameProvider
        get() = DefaultTypedNameProvider(topNames, topNames, middleNames, middleNames)

    override val middle: TypedNameProvider
        get() = DefaultTypedNameProvider(middleNames, middleNames, middleNames, middleNames)

    override val bottom: TypedNameProvider
        get() = DefaultTypedNameProvider(bottomNames, middleNames, middleNames, bottomNames)
}