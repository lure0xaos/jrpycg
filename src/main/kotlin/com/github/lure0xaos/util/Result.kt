package com.github.lure0xaos.util

sealed interface Result<T> {
    val isSuccess: Boolean
    val isFailure: Boolean
    fun getOrElse(onFailure: (Throwable) -> T): T
    fun getOrElse(onFailure: T): T
    fun getOrNull(): T?
    fun getOrThrow(): T?
    fun throwableOrNull(): Throwable?
    fun onSuccess(onSuccess: (T) -> Unit): Result<T>
    fun onFailure(onFailure: (Throwable) -> Unit): Result<T>
    fun fold(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit): Result<T>
    fun recover(onFailure: (Throwable) -> T): Result<T>
    fun <R> map(transform: (T) -> R): Result<R>
    private class Success<T>(private val value: T) : Result<T> {
        override val isSuccess: Boolean = true
        override val isFailure: Boolean = false
        override fun getOrElse(onFailure: T): T = value
        override fun getOrElse(onFailure: (Throwable) -> T): T = value
        override fun getOrNull(): T? = value
        override fun getOrThrow(): T? = value
        override fun throwableOrNull(): Throwable? = null
        override fun onSuccess(onSuccess: (T) -> Unit): Result<T> {
            onSuccess(value)
            return this
        }

        override fun onSuccessCatching(onSuccess: (T) -> Unit): Result<T> {
            try {
                onSuccess(value)
            } catch (e: Throwable) {
                return failure(e)
            }
            return this
        }

        override fun onFailure(onFailure: (Throwable) -> Unit): Result<T> = this
        override fun onFailureCatching(onFailure: (Throwable) -> Unit): Result<T> = this
        override fun <R> map(transform: (T) -> R): Result<R> = success(value.let(transform))
        override fun <R> mapCatching(transform: (T) -> R): Result<R> =
            try {
                success(value.let(transform))
            } catch (e: Throwable) {
                failure(e)
            }

        override fun recover(onFailure: (Throwable) -> T): Result<T> = this
        override fun recoverCatching(onFailure: (Throwable) -> T): Result<T> = this
        override fun fold(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit): Result<T> =
            onSuccess(onSuccess)

        override fun foldCatching(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit): Result<T> =
            try {
                onSuccess(onSuccess)
            } catch (e: Exception) {
                failure(e)
            }
    }

    private class Failure<T>(private val throwable: Throwable) : Result<T> {
        override val isSuccess: Boolean = false
        override val isFailure: Boolean = true
        override fun getOrElse(onFailure: T): T = onFailure
        override fun getOrElse(onFailure: (Throwable) -> T): T = onFailure(throwable)
        override fun getOrNull(): T? = null
        override fun getOrThrow(): T? {
            throw throwable
        }

        override fun throwableOrNull(): Throwable = throwable
        override fun onSuccess(onSuccess: (T) -> Unit): Result<T> = this
        override fun onFailure(onFailure: (Throwable) -> Unit): Result<T> {
            onFailure(throwable)
            return this
        }

        override fun onFailureCatching(onFailure: (Throwable) -> Unit): Result<T> {
            try {
                onFailure(throwable)
            } catch (e: Throwable) {
                return failure(e)
            }
            return this
        }

        override fun foldCatching(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit): Result<T> =
            try {
                onFailure(onFailure)
            } catch (e: Throwable) {
                failure(e)
            }

        override fun recoverCatching(onFailure: (Throwable) -> T): Result<T> {
            try {
                success(onFailure(throwable))
            } catch (e: Throwable) {
                return failure(e)
            }
            return this
        }

        override fun <R> mapCatching(transform: (T) -> R): Result<R> = failure(throwable)
        override fun onSuccessCatching(onSuccess: (T) -> Unit): Result<T> = this
        override fun <R> map(transform: (T) -> R): Result<R> = failure(throwable)
        override fun recover(onFailure: (Throwable) -> T): Result<T> = success(onFailure(throwable))
        override fun fold(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit): Result<T> =
            onFailure(onFailure)
    }

    companion object {
        fun <T> success(obj: T): Result<T> = Success(obj)
        fun <T> failure(Throwable: Throwable): Result<T> = Failure(Throwable)
        inline fun <T> runCatching(action: () -> T): Result<T> =
            try {
                success(action())
            } catch (e: Throwable) {
                failure(e)
            }

        inline fun <O, T> O.runCatching(action: () -> T): Result<T> =
            try {
                success(action())
            } catch (e: Throwable) {
                failure(e)
            }

        fun <T> kotlin.Result<T>.toResult(): Result<T> =
            if (isSuccess) success(getOrNull()!!) else failure(exceptionOrNull()!!)
    }

    fun onSuccessCatching(onSuccess: (T) -> Unit): Result<T>
    fun onFailureCatching(onFailure: (Throwable) -> Unit): Result<T>
    fun <R> mapCatching(transform: (T) -> R): Result<R>
    fun recoverCatching(onFailure: (Throwable) -> T): Result<T>
    fun foldCatching(onSuccess: (T) -> Unit, onFailure: (Throwable) -> Unit): Result<T>
}
