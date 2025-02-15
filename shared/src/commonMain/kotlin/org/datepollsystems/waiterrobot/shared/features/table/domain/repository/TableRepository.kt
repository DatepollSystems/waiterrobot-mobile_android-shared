package org.datepollsystems.waiterrobot.shared.features.table.domain.repository

import org.datepollsystems.waiterrobot.shared.features.table.domain.model.OrderedItem
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table

internal interface TableRepository {
    suspend fun getUnpaidOrderItems(table: Table): Result<List<OrderedItem>>
    suspend fun updateTablesWithOpenOrders(eventId: Long)
}
