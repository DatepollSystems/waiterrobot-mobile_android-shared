package org.datepollsystems.waiterrobot.shared.features.switchevent.repository

import io.sentry.kotlin.multiplatform.Sentry
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.repository.AbstractRepository
import org.datepollsystems.waiterrobot.shared.core.sentry.SentryTag
import org.datepollsystems.waiterrobot.shared.core.sentry.setTag
import org.datepollsystems.waiterrobot.shared.features.switchevent.api.EventLocationApi
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event

internal class SwitchEventRepository(
    private val eventLocationApi: EventLocationApi,
) : AbstractRepository() {

    suspend fun getEvents(): List<Event> = eventLocationApi.getEvents().map {
        Event(
            id = it.id,
            name = it.name,
            startDate = it.startDate,
            endDate = it.endDate,
            city = it.city,
            organisationId = it.organisationId,
            stripeSettings = if (it.stripeEnabled && it.stripeLocationId != null) {
                Event.StripeSettings.Enabled(it.stripeLocationId, it.stripeMinAmount ?: 0)
            } else {
                Event.StripeSettings.Disabled
            }
        )
    }

    suspend fun switchToEvent(event: Event): Boolean {
        val oldEventId = CommonApp.settings.selectedEvent?.id
        CommonApp.settings.selectedEvent = event
        Sentry.configureScope { scope ->
            scope.setTag(SentryTag.EVENT_ID, event.id.toString())
        }

        if (oldEventId != event.id) {
            val stripeProvider = CommonApp.stripeProvider
            if (stripeProvider?.isInitialized() == true) stripeProvider.disconnectReader()

            // Reset ignoring card payment setup
            CommonApp.settings.enableContactlessPayment = true

            return true
        }

        return false
    }
}
