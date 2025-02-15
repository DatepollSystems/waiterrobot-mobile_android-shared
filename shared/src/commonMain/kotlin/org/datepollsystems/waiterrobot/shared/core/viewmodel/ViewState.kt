package org.datepollsystems.waiterrobot.shared.core.viewmodel

import org.datepollsystems.waiterrobot.shared.core.viewmodel.DialogState.Button

sealed class ViewState {
    data object Idle : ViewState()
    data object Loading : ViewState()
    data class Error(val dialog: DialogState) : ViewState() {
        constructor(
            title: String,
            text: String,
            onDismiss: () -> Unit,
            primaryButton: Button,
            secondaryButton: Button? = null,
        ) : this(DialogState(title, text, onDismiss, primaryButton, secondaryButton))
    }
}
