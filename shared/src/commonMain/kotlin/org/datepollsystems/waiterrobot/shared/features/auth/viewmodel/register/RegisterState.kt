package org.datepollsystems.waiterrobot.shared.features.auth.viewmodel.register

import org.datepollsystems.waiterrobot.shared.core.viewmodel.StateWithViewState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState

data class RegisterState(
    override val viewState: ViewState = ViewState.Idle
) : ViewModelState, StateWithViewState
