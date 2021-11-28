package net.rubygrapefruit.gen.builders

/**
 * A container that builds zero or more elements of type T
 */
abstract class ComponentsBuilder<T> {
    private var count = 0
    private var finalContents: List<T>? = null
    private var finalizing = false

    val currentSize: Int
        get() = count

    val contents: List<T>
        get() {
            finalize()
            return finalContents!!
        }

    fun add() {
        assertNotFinalized()
        count++
    }

    protected fun assertFinalized() {
        require(finalContents != null)
    }

    protected fun assertNotFinalized() {
        require(finalContents == null)
    }

    private fun finalize() {
        if (finalContents == null) {
            require(!finalizing)
            finalizing = true
            finalContents = calculateContents(count)
            finalizing = false
        }
    }

    protected abstract fun calculateContents(count: Int): List<T>

}