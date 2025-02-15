package org.datepollsystems.waiterrobot.android.ui.core.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import org.datepollsystems.waiterrobot.android.ui.core.AlertDialogFromState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        // Background content
        Box(modifier = if (isLoading) Modifier.alpha(0.5f) else Modifier) {
            content()
        }

        // Loading overlay
        if (isLoading) {
            LoadingView(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.2f)) // Semi-transparent overlay
                    .clickable(enabled = false) {}, // Disable interactions
            )
        }
    }
}

@Composable
fun ViewStateOverlay(
    modifier: Modifier = Modifier,
    state: ViewState,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialogFromState(state)

    LoadingOverlay(
        modifier = modifier,
        isLoading = state == ViewState.Loading,
    ) {
        Column(content = content)
    }
}

