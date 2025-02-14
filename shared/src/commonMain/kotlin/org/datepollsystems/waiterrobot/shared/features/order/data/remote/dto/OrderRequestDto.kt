package org.datepollsystems.waiterrobot.shared.features.order.data.remote.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.shared.core.data.api.RequestBodyDto

@Serializable
class OrderRequestDto(
    val tableId: Long,
    val products: List<OrderItemDto>,
    val clientOrderId: String,
) : RequestBodyDto {
    @Serializable
    class OrderItemDto(
        val id: Long,
        val amount: Int,
        val note: String?
    )
}
