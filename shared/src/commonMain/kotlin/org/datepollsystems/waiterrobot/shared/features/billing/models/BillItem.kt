package org.datepollsystems.waiterrobot.shared.features.billing.models

import org.datepollsystems.waiterrobot.shared.utils.Money
import org.datepollsystems.waiterrobot.shared.utils.times

// TODO API should also return the note otherwise it does not make sense to get it split,
//  as there wouldn't be a difference in the UI between items with the same baseProductId
data class BillItem(
    val baseProductId: Long,
    val name: String,
    val ordered: Int,
    val selectedForBill: Int,
    val pricePerPiece: Money,
    internal val orderProductIds: List<Long> = emptyList(),
) {
    val priceSum: Money get() = selectedForBill * pricePerPiece
}
