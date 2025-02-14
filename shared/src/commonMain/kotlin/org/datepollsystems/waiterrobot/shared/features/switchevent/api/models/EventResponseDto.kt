package org.datepollsystems.waiterrobot.shared.features.switchevent.api.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
internal data class EventResponseDto(
    val id: Long,
    val name: String,
    val startDate: Instant? = null,
    val endDate: Instant? = null,
    val street: String,
    val city: String,
    val streetNumber: String,
    val postalCode: String,
    val organisationId: Long,
    val stripeEnabled: Boolean,
    val stripeMinAmount: Int? = null, // null when stripe is disabled
    val stripeLocationId: String? = null, // null when stripe is disabled
    val isDemo: Boolean,
)
