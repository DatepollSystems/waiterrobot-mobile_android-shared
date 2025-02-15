package org.datepollsystems.waiterrobot.shared.core.viewmodel

sealed class ViewState {
    data object Idle : ViewState()
    data object Loading : ViewState()
    data class ErrorDialog(val dialog: DialogState) : ViewState()

    @Deprecated("Use ErrorDialog instead")
    data class Error(
        val title: String,
        val message: String,
        val onDismiss: () -> Unit
    ) : ViewState()
}
