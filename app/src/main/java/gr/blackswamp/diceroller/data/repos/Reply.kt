package gr.blackswamp.diceroller.data.repos


@Suppress("unused")
class Reply<out T>(private val value: Any?) {
    val isSuccess: Boolean get() = value !is Failure

    val isFailure: Boolean get() = value is Failure
    fun getOrNull(): T? =
        when {
            isFailure -> null
            else -> value as T
        }


    fun exceptionOrNull(): Throwable? =
        when (value) {
            is Failure -> value.exception
            else -> null
        }

    override fun toString(): String =
        when (value) {
            is Failure -> value.toString()
            else -> "Success($value)"
        }

    // companion with constructors

    /**
     * Companion object for [Reply] class that contains its constructor functions
     * [success] and [failure].
     */
    companion object {

        fun <T> success(value: T): Reply<T> =
            Reply(value)

        fun <T> failure(exception: Throwable): Reply<T> =
            Reply(Failure(exception))
    }

    internal class Failure(val exception: Throwable) {
        override fun equals(other: Any?): Boolean = other is Failure && exception == other.exception
        override fun hashCode(): Int = exception.hashCode()
        override fun toString(): String = "Failure($exception)"
    }
}
