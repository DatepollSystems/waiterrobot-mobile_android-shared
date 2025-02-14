package org.datepollsystems.waiterrobot.shared.features.order.domain.repository

import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.data.api.ID
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.ProductGroup

interface ProductGroupRepository {
    fun getProductGroups(eventId: Long, filter: String?): Flow<Resource<List<ProductGroup>>>
    suspend fun refreshProductGroups(eventId: ID): Result<Unit>
}
