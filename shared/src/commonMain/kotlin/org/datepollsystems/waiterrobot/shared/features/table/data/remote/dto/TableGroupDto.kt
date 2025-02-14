package org.datepollsystems.waiterrobot.shared.features.table.data.remote.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.TableGroupEntity

@Serializable
internal data class TableGroupDto(
    val id: Long,
    val eventId: Long,
    val name: String,
    val position: Int = Int.MAX_VALUE,
    val color: String?,
    val tables: List<TableDto>,
) {
    fun toEntry(timestamp: Instant) = TableGroupEntity(
        id = this.id,
        name = this.name,
        eventId = this.eventId,
        position = this.position,
        color = this.color,
        tables = this.tables.map(TableDto::toEntry),
        updatedAt = timestamp
    )
}
