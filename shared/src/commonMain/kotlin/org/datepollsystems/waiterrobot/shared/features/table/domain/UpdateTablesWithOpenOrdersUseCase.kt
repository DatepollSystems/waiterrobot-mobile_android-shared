package org.datepollsystems.waiterrobot.shared.features.table.domain

import org.datepollsystems.waiterrobot.shared.core.data.AbstractUseCase
import org.datepollsystems.waiterrobot.shared.core.data.EventProvider
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableRepository
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable

internal class UpdateTablesWithOpenOrdersUseCase(
    private val tableRepository: TableRepository,
    private val eventProvider: EventProvider
) : AbstractUseCase() {
    suspend operator fun invoke(): Result<Unit> = runCatchingCancelable {
        val event = eventProvider.value ?: return@runCatchingCancelable
        tableRepository.updateTablesWithOpenOrders(event.id)
    }
}
