package org.datepollsystems.waiterrobot.shared.features.order.data

import org.datepollsystems.waiterrobot.shared.core.repository.AbstractRepository
import org.datepollsystems.waiterrobot.shared.features.order.data.remote.OrderApi
import org.datepollsystems.waiterrobot.shared.features.order.data.remote.dto.OrderRequestDto
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.OrderItem
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.OrderRepository
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable

internal class OrderRepositoryImpl(
    private val orderApi: OrderApi
) : OrderRepository, AbstractRepository() {

    override suspend fun sendOrder(
        table: Table,
        order: Collection<OrderItem>,
        orderId: String
    ) = runCatchingCancelable { // TODO or should this catch be in the UseCase?
        val items = order.map { OrderRequestDto.OrderItemDto(it.product.id, it.amount, it.note) }
        orderApi.sendOrder(OrderRequestDto(table.id, items, orderId))
    }
}
