package org.datepollsystems.waiterrobot.shared.features.table.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import org.datepollsystems.waiterrobot.shared.core.data.AbstractUseCase
import org.datepollsystems.waiterrobot.shared.core.data.EventProvider
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableGroupRepository
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable

internal class RefreshTableGroupsUseCase(
    private val tableGroupRepository: TableGroupRepository,
    private val eventProvider: EventProvider,
) : AbstractUseCase() {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(): Result<Unit> {
        val result = eventProvider.flow.mapLatest { event ->
            runCatchingCancelable { // TODO move to Repository?
                if (event == null) return@runCatchingCancelable
                tableGroupRepository.refreshTableGroups(event.id)
            }
        }.first()

        return result
    }
}
