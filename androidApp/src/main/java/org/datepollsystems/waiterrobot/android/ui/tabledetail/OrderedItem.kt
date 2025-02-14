package org.datepollsystems.waiterrobot.android.ui.tabledetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.OrderedItem

@Composable
fun OrderedItem(item: OrderedItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.weight(0.15f),
            textAlign = TextAlign.Right,
            text = "${item.amount} x"
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            modifier = Modifier.weight(0.85f),
            text = item.name
        )
    }
}
