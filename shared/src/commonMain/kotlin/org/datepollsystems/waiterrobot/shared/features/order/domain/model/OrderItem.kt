package org.datepollsystems.waiterrobot.shared.features.order.domain.model

import org.datepollsystems.waiterrobot.shared.features.product.domain.model.Product

data class OrderItem(
    val product: Product,
    val amount: Int,
    val note: String?
)
