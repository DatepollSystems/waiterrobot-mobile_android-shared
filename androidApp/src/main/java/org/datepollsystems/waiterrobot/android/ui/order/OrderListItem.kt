package org.datepollsystems.waiterrobot.android.ui.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import org.datepollsystems.waiterrobot.android.ui.common.SwipeableListItem
import org.datepollsystems.waiterrobot.android.ui.core.theme.WaiterRobotTheme

@Composable
fun OrderListItem(
    id: Long,
    name: String,
    amount: Int,
    note: String?,
    addAction: (id: Long, amount: Int) -> Unit,
    onLongClick: () -> Unit
) = SwipeableListItem(
    swipeAdd = { addAction(id, 1) },
    swipeRemove = { addAction(id, -1) },
    onClick = { addAction(id, 1) },
    onLongClick = onLongClick
) {
    Column {
        Row {
            Text(
                modifier = Modifier.weight(0.15f),
                textAlign = TextAlign.Right,
                text = "$amount x"
            )
            Spacer(modifier = Modifier.weight(0.1f))
            Text(
                modifier = Modifier.weight(0.7f),
                text = name
            )
        }
        if (note != null) {
            Row {
                Spacer(modifier = Modifier.weight(0.25f))
                Text(
                    modifier = Modifier.weight(0.7f),
                    text = note,
                    fontSize = MaterialTheme.typography.caption.fontSize
                )
            }
        }
    }
}

@Preview
@Composable
private fun OrderListItemPreview() {
    WaiterRobotTheme {
        OrderListItem(
            id = 1,
            name = "Beer",
            amount = 10,
            note = "test Note",
            addAction = { _, _ -> },
            onLongClick = {}
        )
    }
}
