package org.datepollsystems.waiterrobot.shared.features.switchevent.viewmodel

import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.viewmodel.AbstractViewModel
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event
import org.datepollsystems.waiterrobot.shared.features.switchevent.repository.SwitchEventRepository
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce

class SwitchEventViewModel internal constructor(
    private val repository: SwitchEventRepository,
) : AbstractViewModel<SwitchEventState, SwitchEventEffect>(SwitchEventState()) {

    override suspend fun onCreate() {
        loadEvents()
    }

    override suspend fun onUnhandledException(exception: Throwable) {
        TODO("Not yet implemented")
    }

    fun loadEvents() = intent {
        reduce { state.copy(events = Resource.Loading(state.events.data)) }
        repository.getEvents()
            .onSuccess { events ->
                reduce { state.copy(events = Resource.Success(events)) }
            }
            .onFailure { exception ->
                reduce { state.copy(events = Resource.Error(exception, state.events.data)) }
            }
    }

    fun onEventSelected(event: Event) = intent {
        val needToPop = CommonApp.selectedEvent.value != null
        repository.switchToEvent(event)

        if (needToPop) navigator.pop()
    }

    fun logout() = intent {
        CommonApp.logout()
    }
}
