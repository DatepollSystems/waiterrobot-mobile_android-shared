package org.datepollsystems.waiterrobot.shared.features.table.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class UnpaidTableIdsDto(
    val tableIds: Set<Long>
)
