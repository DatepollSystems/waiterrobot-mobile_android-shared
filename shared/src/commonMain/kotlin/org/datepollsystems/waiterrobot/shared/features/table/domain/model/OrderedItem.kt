package org.datepollsystems.waiterrobot.shared.features.table.domain.model

data class OrderedItem(
    val baseProductId: Long,
    val name: String,
    val amount: Int,
    val virtualId: Long,
    val note: String,
)
