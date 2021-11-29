package net.rubygrapefruit.gen.builders

/**
 * A container that builds zero or more elements of type T
 */
abstract class ComponentsBuilder<T> {
    private var finalContents: List<T>? = null
    private var finalizing = false

    abstract val currentSize: Int

    val contents: List<T>
        get() {
            finalize()
            return finalContents!!
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
            finalContents = calculateContents()
            finalizing = false
        }
    }

    protected abstract fun calculateContents(): List<T>

}