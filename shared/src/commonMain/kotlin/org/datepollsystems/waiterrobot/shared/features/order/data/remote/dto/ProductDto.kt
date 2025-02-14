package org.datepollsystems.waiterrobot.shared.features.order.data.remote.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.ProductEntity
import org.datepollsystems.waiterrobot.shared.utils.Cents

@Serializable
internal class ProductDto(
    val id: Long,
    val name: String,
    val soldOut: Boolean,
    val price: Cents,
    val color: String?,
    val allergens: List<AllergenDto>,
    val position: Int = Int.MAX_VALUE,
) {
    fun toEntry() = ProductEntity(
        id = this.id,
        name = this.name,
        price = this.price,
        soldOut = this.soldOut,
        color = this.color,
        allergens = this.allergens.map(AllergenDto::toEntry),
        position = this.position,
    )
}
