package org.datepollsystems.waiterrobot.shared.features.table.domain.model

data class GroupedTables(
    val id: Long,
    val name: String,
    val eventId: Long,
    val color: String?,
    val tables: List<Table>,
)
