package org.datepollsystems.waiterrobot.shared.core.data

import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event

internal interface EventProvider {
    val value: Event?
    val flow: Flow<Event?>
}

internal class SettingsEventProvider : EventProvider {
    override val value: Event? get() = CommonApp.settings.selectedEvent
    override val flow: Flow<Event?> = CommonApp.settings.selectedEventFlow
}
