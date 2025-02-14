package org.datepollsystems.waiterrobot.shared.features.order.domain.model

import org.datepollsystems.waiterrobot.shared.utils.Money

data class Product(
    val id: Long,
    val name: String,
    val price: Money,
    val soldOut: Boolean,
    val color: String?,
    val allergens: List<Allergen>,
    val position: Int,
)
