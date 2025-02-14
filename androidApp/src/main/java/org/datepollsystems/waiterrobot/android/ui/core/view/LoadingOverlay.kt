package org.datepollsystems.waiterrobot.android.ui.core.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(modifier = modifier) {
        // Background content
        Box(modifier = if (isLoading) Modifier.alpha(0.5f) else Modifier) {
            content()
        }

        // Loading overlay
        if (isLoading) {
            LoadingView(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f)) // Semi-transparent overlay
                    .clickable(enabled = false) {}, // Disable interactions
            )
        }
    }
}
