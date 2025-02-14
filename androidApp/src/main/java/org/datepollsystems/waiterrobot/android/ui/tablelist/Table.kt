package org.datepollsystems.waiterrobot.android.ui.tablelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.android.ui.core.Preview
import org.datepollsystems.waiterrobot.android.util.bestContrastColor
import org.datepollsystems.waiterrobot.android.util.desaturateOnDarkMode
import org.datepollsystems.waiterrobot.android.util.getContentColor
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table

@Composable
fun Table(table: Table, color: Color?, onClick: (Table) -> Unit) {
    Card(
        onClick = { onClick(table) },
        modifier = Modifier.aspectRatio(1f)
    ) {
        val backgroundColor = color?.desaturateOnDarkMode()
        val textColor = backgroundColor?.getContentColor() ?: Color.Unspecified
        Box(
            modifier = Modifier
                .background(backgroundColor ?: Color.Unspecified)
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = table.number.toString(),
                color = textColor,
                style = MaterialTheme.typography.headlineMedium,
            )
            if (table.hasOrders) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd),
                    containerColor = backgroundColor?.bestContrastColor(
                        color1 = MaterialTheme.colorScheme.error,
                        color2 = MaterialTheme.colorScheme.errorContainer
                    )?.desaturateOnDarkMode() ?: MaterialTheme.colorScheme.error
                ) {} // content != null -> large Badge
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TablePreview() = Preview {
    Table(
        table = Table(0, 1, "Group 1", hasOrders = true),
        color = Color.Red,
        onClick = {}
    )
}
