package org.datepollsystems.waiterrobot.shared.core.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.datepollsystems.waiterrobot.shared.utils.extensions.Now
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable
import org.koin.core.component.KoinComponent

internal abstract class AbstractRepository : KoinComponent {
    protected val logger: Logger by injectLoggerForClass()

    /**
     * Handles resources that come from a remote source (e.g. REST-API) and should **not** be cached locally.
     *
     * @param fetch the fetch operation to execute
     */
    internal fun <Model> remoteResource(
        initial: Model? = null,
        fetch: suspend () -> Model,
    ): Flow<Resource<Model>> = flow {
        emit(Resource.Loading(initial))

        runCatchingCancelable {
            val result = fetch()
            emit(Resource.Success(result))
        }.onFailure { e ->
            logger.i(e) { "Fetching remoteResource failed" }
            emit(Resource.Error(e, initial))
        }
    }

    protected fun <Entity, Model> cached(
        query: () -> Flow<List<Entity>>,
        shouldRefresh: Entity.(now: Instant) -> Boolean,
        refresh: suspend () -> Unit,
        transform: (List<Entity>) -> List<Model>
    ): Flow<Resource<List<Model>>> = flow {
        val now = Now()
        val cached = query().first()

        if (cached.isEmpty() || cached.any { it.shouldRefresh(now) }) {
            emit(Resource.Loading(transform(cached)))

            runCatchingCancelable {
                refresh() // Result will be emitted by the emitAll at the end
            }.onFailure { e ->
                emit(Resource.Error(e, transform(cached)))
            }
        }

        emitAll(query().map { Resource.Success(transform(it)) })
    }
}
