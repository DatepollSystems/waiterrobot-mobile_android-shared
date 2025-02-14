package org.datepollsystems.waiterrobot.shared.features.switchevent.viewmodel

import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event

data class SwitchEventState(
    val events: Resource<List<Event>> = Resource.Loading()
) : ViewModelState
