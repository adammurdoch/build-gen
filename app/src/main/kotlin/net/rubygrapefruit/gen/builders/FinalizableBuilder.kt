package net.rubygrapefruit.gen.builders

abstract class FinalizableBuilder<T> {
    private enum class State {
        Mutating, FinalizeOnRead, Finalizing, Finalized
    }

    private var finalValue: T? = null
    private var state = State.Mutating

    protected val value: T
        get() {
            maybeFinalize()
            return finalValue!!
        }

    protected fun assertFinalized() {
        require(state == State.Finalized)
    }

    protected fun assertCanMutate() {
        require(state == State.Mutating)
    }

    protected fun finalizeOnRead() {
        if (state == State.Mutating) {
            state = State.FinalizeOnRead
        }
    }

    private fun maybeFinalize() {
        if (state == State.FinalizeOnRead) {
            finalized()
        } else {
            assertFinalized()
        }
    }

    protected fun finalized(): T {
        if (state != State.Finalized) {
            require(state == State.Mutating || state == State.FinalizeOnRead)
            state = State.Finalizing
            finalValue = calculateValue()
            state = State.Finalized
        }
        return finalValue!!
    }

    protected abstract fun calculateValue(): T
}