package org.datepollsystems.waiterrobot.shared.features.order.domain.model

data class OrderItem(
    val product: Product,
    val amount: Int,
    val note: String?
)
