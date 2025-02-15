package org.datepollsystems.waiterrobot.shared.features.product.domain.repository

import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.GroupedProducts

interface ProductGroupRepository {
    fun getProductGroups(eventId: Long, filter: String?): Flow<Resource<List<GroupedProducts>>>
    suspend fun refreshProductGroups(eventId: Long): Result<Unit>
}
