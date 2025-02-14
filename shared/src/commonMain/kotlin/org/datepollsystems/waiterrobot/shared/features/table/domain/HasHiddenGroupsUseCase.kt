package org.datepollsystems.waiterrobot.shared.features.table.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest
import org.datepollsystems.waiterrobot.shared.core.data.AbstractUseCase
import org.datepollsystems.waiterrobot.shared.core.data.EventProvider
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableGroupRepository

internal class HasHiddenGroupsUseCase(
    private val tableGroupRepository: TableGroupRepository,
    private val eventProvider: EventProvider,
) : AbstractUseCase() {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<Boolean> =
        eventProvider.flow.transformLatest { event ->
            if (event == null) {
                emit(false)
                return@transformLatest
            }

            emitAll(tableGroupRepository.hasHiddenGroups(event.id))
        }
}
