package org.datepollsystems.waiterrobot.shared.root

import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState
import org.datepollsystems.waiterrobot.shared.features.settings.models.AppTheme

data class RootState(
    val selectedTheme: AppTheme = CommonApp.appTheme.value,
    val isLoggedIn: Boolean = CommonApp.isLoggedIn.value,
    val eventSelected: Boolean = CommonApp.selectedEvent.value != null,
    val viewState: ViewState = ViewState.Idle
) : ViewModelState
