package org.datepollsystems.waiterrobot.shared.features.table.data.remote.dto

import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.TableEntity

@Serializable
internal data class TableDto(
    val id: Long,
    val number: Int,
) {
    fun toEntry() = TableEntity(
        id = this.id,
        number = this.number,
    )
}
