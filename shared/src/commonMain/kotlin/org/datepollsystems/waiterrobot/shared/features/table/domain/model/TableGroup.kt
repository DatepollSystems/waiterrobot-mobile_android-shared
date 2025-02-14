package org.datepollsystems.waiterrobot.shared.features.table.domain.model

data class TableGroup(
    val id: Long,
    val name: String,
    val color: String?,
    val hidden: Boolean,
)
