package org.datepollsystems.waiterrobot.android.ui.billing

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.android.ui.common.CenteredText
import org.datepollsystems.waiterrobot.android.ui.common.SwipeableListItem
import org.datepollsystems.waiterrobot.android.ui.core.Preview
import org.datepollsystems.waiterrobot.shared.features.billing.models.BillItem
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.noOpenBill
import org.datepollsystems.waiterrobot.shared.utils.euro

@Composable
fun BillList(
    table: Table,
    billItems: List<BillItem>,
    addAction: (id: Long, amount: Int) -> Unit
) {
    Surface {
        Column {
            if (billItems.isEmpty()) {
                CenteredText(
                    text = L.billing.noOpenBill(table.groupName, table.number.toString()),
                    scrollAble = true
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(billItems, key = BillItem::baseProductId) { billItem ->
                        BillListItem(
                            item = billItem,
                            addAction = addAction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BillListItem(
    item: BillItem,
    modifier: Modifier = Modifier,
    addAction: (id: Long, amount: Int) -> Unit
) = SwipeableListItem(
    modifier = modifier,
    swipeAdd = { addAction(item.baseProductId, 1) },
    swipeRemove = { addAction(item.baseProductId, -1) },
    onClick = { addAction(item.baseProductId, 1) }
) {
    Text(
        modifier = Modifier.weight(0.2f),
        textAlign = TextAlign.Right,
        text = "${item.ordered}x"
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
        modifier = Modifier.weight(0.6f),
        text = item.name
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
        modifier = Modifier.weight(0.15f),
        textAlign = TextAlign.Right,
        text = item.selectedForBill.toString()
    )
    Spacer(modifier = Modifier.width(16.dp))
    Text(
        modifier = Modifier.weight(0.25f),
        textAlign = TextAlign.Right,
        text = item.priceSum.toString()
    )
}

@PreviewLightDark
@Composable
private fun BillListPreview() = Preview {
    BillList(
        table = Table(1, 1, "Outside", false),
        billItems = listOf(
            BillItem(1, "Beer", 10, 5, 4.euro),
            BillItem(2, "Fries", 5, 0, 3.euro),
        ),
        addAction = { _, _ -> }
    )
}

@PreviewLightDark
@Composable
private fun BillListItemPreview() = Preview {
    BillListItem(
        item = BillItem(1, "Beer", 10, 5, 4.euro),
        addAction = { _, _ -> }
    )
}
