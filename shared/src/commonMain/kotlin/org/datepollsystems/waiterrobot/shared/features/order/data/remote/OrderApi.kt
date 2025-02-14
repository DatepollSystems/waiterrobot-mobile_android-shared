package org.datepollsystems.waiterrobot.shared.features.order.data.remote

import org.datepollsystems.waiterrobot.shared.core.data.api.AuthorizedApi
import org.datepollsystems.waiterrobot.shared.core.data.api.AuthorizedClient
import org.datepollsystems.waiterrobot.shared.features.order.data.remote.dto.OrderRequestDto

internal class OrderApi(client: AuthorizedClient) : AuthorizedApi("v1/waiter/order", client) {
    suspend fun sendOrder(order: OrderRequestDto) {
        post("/", order)
    }
}
