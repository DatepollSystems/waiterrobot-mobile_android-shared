package org.datepollsystems.waiterrobot.shared.features.order.data.remote.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.AllergenEntity

@Serializable
internal class AllergenDto(
    val id: Long,
    val name: String,
    val shortName: String
) {
    fun toEntry() = AllergenEntity(
        id = this.id,
        name = this.name,
        shortName = this.shortName
    )
}
