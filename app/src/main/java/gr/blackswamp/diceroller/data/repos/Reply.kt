package gr.blackswamp.diceroller.data.repos

import androidx.annotation.StringRes

sealed class Reply<out T : Any> {
    companion object {
        fun success(): Reply<Unit> =
            Success(Unit)
    }

    data class Success<T : Any>(val data: T) : Reply<T>()
    data class Failure(@StringRes val messageId: Int, val exception: Throwable) : Reply<Nothing>()

    val hasError get() = this is Failure

    fun onSuccess(block: (T) -> Unit): Reply<T> {
        if (this is Success<T>) {
            block.invoke(this.data)
        }
        return this
    }

    fun onFailure(block: (Failure) -> Unit): Reply<T> {
        if (this is Failure) {
            block.invoke(this)
        }
        return this
    }

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Failure -> "Error[throwable=$exception]"
        }
    }

}
