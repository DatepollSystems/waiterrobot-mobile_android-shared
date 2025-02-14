package org.datepollsystems.waiterrobot.shared.features.order.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import org.datepollsystems.waiterrobot.shared.core.data.AbstractUseCase
import org.datepollsystems.waiterrobot.shared.core.data.EventProvider
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.ProductGroupRepository

internal class RefreshProductGroupsUseCase(
    private val productGroupRepository: ProductGroupRepository,
    private val eventProvider: EventProvider,
) : AbstractUseCase() {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(): Result<Unit> {
        val result = eventProvider.flow.mapLatest { event ->
            if (event == null) return@mapLatest Result.success(Unit) // TODO emit Result.Error?
            productGroupRepository.refreshProductGroups(event.id)
        }.first()

        return result
    }
}
