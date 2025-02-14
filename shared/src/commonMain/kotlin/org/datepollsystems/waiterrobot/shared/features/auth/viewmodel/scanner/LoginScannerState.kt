package org.datepollsystems.waiterrobot.shared.features.auth.viewmodel.scanner

import org.datepollsystems.waiterrobot.shared.core.viewmodel.StateWithViewState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState

data class LoginScannerState(
    override val viewState: ViewState = ViewState.Idle
) : ViewModelState, StateWithViewState
