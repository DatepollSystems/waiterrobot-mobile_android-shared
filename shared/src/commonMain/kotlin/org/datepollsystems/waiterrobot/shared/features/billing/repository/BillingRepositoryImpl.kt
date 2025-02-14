package org.datepollsystems.waiterrobot.shared.features.billing.repository

import org.datepollsystems.waiterrobot.shared.core.repository.AbstractRepository
import org.datepollsystems.waiterrobot.shared.features.billing.api.BillingApi
import org.datepollsystems.waiterrobot.shared.features.billing.models.BillItem
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable

internal class BillingRepositoryImpl(
    private val billingApi: BillingApi,
) : BillRepository, AbstractRepository() {
    suspend fun getBillForTable(
        table: Table,
        selectAll: Boolean,
    ): Result<Map<Long, BillItem>> = runCatchingCancelable {
        billingApi.getBillForTable(table.id)
            .getBillItems(selectAll)
            .associateBy(BillItem::baseProductId)
    }

    suspend fun payBill(
        table: Table,
        items: List<BillItem>,
        selectAll: Boolean
    ): Result<Map<Long, BillItem>> = runCatchingCancelable {
        billingApi.payBill(
            table.id,
            items.flatMap { it.orderProductIds.take(it.selectedForBill) }
        ).openBill
            .getBillItems(selectAll)
            .associateBy(BillItem::baseProductId)
    }
}
