package org.datepollsystems.waiterrobot.shared.features.order.domain.repository

import org.datepollsystems.waiterrobot.shared.features.order.domain.model.OrderItem
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table

interface OrderRepository {
    suspend fun sendOrder(table: Table, order: Collection<OrderItem>, orderId: String): Result<Unit>
}
