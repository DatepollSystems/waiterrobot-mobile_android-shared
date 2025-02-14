package org.datepollsystems.waiterrobot.shared.features.table.domain.model

import kotlinx.serialization.Serializable

@Serializable // needed for android navigation
data class Table(
    val id: Long,
    val number: Int,
    val groupName: String,
    val hasOrders: Boolean = false
)
