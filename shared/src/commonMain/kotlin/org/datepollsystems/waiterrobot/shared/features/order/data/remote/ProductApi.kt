package org.datepollsystems.waiterrobot.shared.features.order.data.remote

import io.ktor.client.call.body
import org.datepollsystems.waiterrobot.shared.core.data.api.AuthorizedApi
import org.datepollsystems.waiterrobot.shared.core.data.api.AuthorizedClient
import org.datepollsystems.waiterrobot.shared.features.order.data.remote.dto.ProductGroupDto

internal class ProductApi(client: AuthorizedClient) : AuthorizedApi("v1/waiter/product", client) {

    suspend fun getProducts(eventId: Long) =
        get("/", "eventId" to eventId.toString()).body<List<ProductGroupDto>>()
}
