package org.datepollsystems.waiterrobot.shared.core.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.koin.core.component.KoinComponent
import kotlin.coroutines.cancellation.CancellationException

internal abstract class AbstractRepository : KoinComponent {
    protected val logger: Logger by injectLoggerForClass()

    /**
     * Handles resources that come from a remote source (e.g. REST-API) and should be cached locally.
     *
     * Always tries to load from cache first and then check with the [shouldFetch] function,
     * if the cached data is stale and should be refreshed. If that's the case [update] will be called
     * and a [Resource.Loading] with the cached data gets emitted.
     * [update] should fetch the new data and update the cache. If [update] fails a [Resource.Error]
     * with the cached data and the exception is emitted.
     * Each change to the cache will be emitted.
     *
     * @param query responsible for subscribing to the cache and getting a flow of a database query
     * @param update responsible for fetching the newest data from a remote source and inserting it into the database.
     *               Only executed if [shouldFetch] returns true.
     * @param shouldFetch decides if the data from the database are stale
     * @param mapDbEntity mapper that maps the [EntityType] to [ModelType]
     */
    internal inline fun <ModelType, EntityType> cachedRemoteResource(
        crossinline query: () -> Flow<EntityType>,
        crossinline update: suspend () -> Unit,
        crossinline shouldFetch: (EntityType) -> Boolean = { true },
        crossinline mapDbEntity: (EntityType) -> ModelType,
    ): Flow<Resource<ModelType>> = flow {
        val cachedData = query().first()

        if (shouldFetch(cachedData)) {
            emit(Resource.Loading(mapDbEntity(cachedData)))

            @Suppress("TooGenericExceptionCaught")
            try {
                update()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.i(e) { "Update of cachedRemoteResource failed" }
                emit(Resource.Error(e, mapDbEntity(cachedData)))
            }
        }

        val flow = query().map { Resource.Success(mapDbEntity(it)) }
        emitAll(flow)
    }

    /**
     * Handles resources that come from a remote source (e.g. REST-API) and should **not** be cached locally.
     *
     * @param fetch the fetch operation to execute
     */
    internal fun <ModelType> remoteResource(
        fetch: suspend () -> ModelType,
    ): Flow<Resource<ModelType>> = flow {
        emit(Resource.Loading(null))

        @Suppress("TooGenericExceptionCaught")
        try {
            val result = fetch()
            emit(Resource.Success(result))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.i(e) { "Fetching remoteResource failed" }
            emit(Resource.Error(e, null))
        }
    }
}
