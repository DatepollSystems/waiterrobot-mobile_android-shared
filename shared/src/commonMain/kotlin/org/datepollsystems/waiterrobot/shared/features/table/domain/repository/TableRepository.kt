package org.datepollsystems.waiterrobot.shared.features.table.domain.repository

import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.OrderedItem
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table

internal interface TableRepository {
    fun getUnpaidOrderItems(table: Table): Flow<Resource<List<OrderedItem>>>
    suspend fun updateTablesWithOpenOrders(eventId: Long)
}
