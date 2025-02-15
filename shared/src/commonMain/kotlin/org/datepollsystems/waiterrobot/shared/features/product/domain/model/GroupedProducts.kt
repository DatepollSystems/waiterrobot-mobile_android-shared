package org.datepollsystems.waiterrobot.shared.features.product.domain.model

data class GroupedProducts(
    val id: Long,
    val name: String,
    val position: Int,
    val color: String?,
    val products: List<Product>
)
