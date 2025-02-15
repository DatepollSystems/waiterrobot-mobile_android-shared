package org.datepollsystems.waiterrobot.android.ui.core

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.datepollsystems.waiterrobot.shared.core.viewmodel.DialogState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.ok

@Composable
fun AlertDialogFromState(dialog: DialogState?) {
    if (dialog == null) return
    AlertDialog(
        onDismissRequest = dialog.onDismiss,
        title = { Text(text = dialog.title) },
        text = { Text(text = dialog.text) },
        confirmButton = {
            Button(onClick = dialog.primaryButton.action) {
                Text(text = dialog.primaryButton.text)
            }
        },
        dismissButton = dialog.secondaryButton?.let { button ->
            {
                TextButton(onClick = button.action) {
                    Text(text = button.text)
                }
            }
        }
    )
}

@Composable
fun AlertDialogFromState(state: ViewState) {
    when (state) {
        is ViewState.ErrorDialog -> AlertDialogFromState(state.dialog)
        is ViewState.Error -> AlertDialog(
            onDismissRequest = state.onDismiss,
            title = { Text(text = state.title) },
            text = { Text(text = state.message) },
            confirmButton = {
                Button(onClick = state.onDismiss) {
                    Text(text = L.dialog.ok())
                }
            }
        )

        else -> return
    }
}
