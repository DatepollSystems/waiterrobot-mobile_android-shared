package org.datepollsystems.waiterrobot.shared.core.data

import org.datepollsystems.waiterrobot.shared.utils.getLocalizedUserMessage

sealed class Resource<T> {
    abstract val data: T?

    internal fun loading(): Loading<T> = when (this) {
        is Loading -> this
        else -> Loading(data)
    }

    internal fun error(exception: Throwable): Error<T> = Error(exception, data)

    internal fun withDefaultData(default: Resource<T>): Resource<T> = withDefaultData(default.data)
    internal fun withDefaultData(default: T?): Resource<T> {
        if (data != null) return this
        return when (this) {
            is Error -> this.copy(data = default)
            is Loading -> this.copy(data = default)
            is Success -> this
        }
    }

    internal fun internalCopy(data: T): Resource<T> = when (this) {
        is Error -> copy(data = data)
        is Loading -> copy(data = data)
        is Success -> copy(data = data)
    }

    data class Loading<T>(override val data: T? = null) : Resource<T>() {
        constructor(resource: Resource<T>) : this(resource.data)
    }

    data class Success<T>(override val data: T) : Resource<T>()
    data class Error<T>(val userMessage: String, override val data: T? = null) : Resource<T>() {
        constructor(
            exception: Throwable,
            data: T? = null
        ) : this(exception.getLocalizedUserMessage(), data)

        constructor(
            exception: Throwable,
            resource: Resource<T>
        ) : this(exception.getLocalizedUserMessage(), resource.data)
    }
}

inline fun <reified T> Resource<List<T>>.objCArray(): Lazy<Resource<Array<T>>> = lazy {
    when (this) {
        is Resource.Error -> Resource.Error(userMessage, data?.toTypedArray())
        is Resource.Loading -> Resource.Loading(data?.toTypedArray())
        is Resource.Success -> Resource.Success(data.toTypedArray())
    }
}

inline fun <K, reified V> Resource<Map<K, V>>.asListResource(): Lazy<Resource<List<V>>> = lazy {
    when (this) {
        is Resource.Error -> Resource.Error(userMessage, data?.values?.toList())
        is Resource.Loading -> Resource.Loading(data?.values?.toList())
        is Resource.Success -> Resource.Success(data.values.toList())
    }
}
