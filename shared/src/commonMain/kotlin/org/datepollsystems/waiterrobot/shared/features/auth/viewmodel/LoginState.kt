package org.datepollsystems.waiterrobot.shared.features.auth.viewmodel

import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState

data class LoginState(
    val viewState: ViewState = ViewState.Idle
) : ViewModelState
