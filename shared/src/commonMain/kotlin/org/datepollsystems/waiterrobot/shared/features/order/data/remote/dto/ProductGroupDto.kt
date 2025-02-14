package org.datepollsystems.waiterrobot.shared.features.order.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.ProductGroupEntity

@Serializable
internal class ProductGroupDto(
    val id: Long,
    val name: String,
    val color: String?,
    val products: List<ProductDto>,
    val position: Int = Int.MAX_VALUE,
) {
    fun toEntry(eventId: Long, timestamp: Instant) = ProductGroupEntity(
        id = this.id,
        name = this.name,
        eventId = eventId,
        position = this.position,
        color = this.color,
        products = this.products.map(ProductDto::toEntry),
        updatedAt = timestamp
    )
}
