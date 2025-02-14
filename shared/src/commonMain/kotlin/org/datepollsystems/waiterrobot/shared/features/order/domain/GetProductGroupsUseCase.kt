package org.datepollsystems.waiterrobot.shared.features.order.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest
import org.datepollsystems.waiterrobot.shared.core.data.AbstractUseCase
import org.datepollsystems.waiterrobot.shared.core.data.EventProvider
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.ProductGroup
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.ProductGroupRepository

internal class GetProductGroupsUseCase(
    private val productGroupRepository: ProductGroupRepository,
    private val eventProvider: EventProvider,
) : AbstractUseCase() {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(filter: String?): Flow<Resource<List<ProductGroup>>> =
        eventProvider.flow.transformLatest { event ->
            if (event == null) return@transformLatest // TODO emit Resource.Error?

            emitAll(productGroupRepository.getProductGroups(event.id, filter))
        }
}

