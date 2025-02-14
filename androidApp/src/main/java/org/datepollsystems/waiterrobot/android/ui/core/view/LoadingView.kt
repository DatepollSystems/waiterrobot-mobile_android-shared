package org.datepollsystems.waiterrobot.android.ui.core.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = Modifier
        .fillMaxSize()
        .then(modifier), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
