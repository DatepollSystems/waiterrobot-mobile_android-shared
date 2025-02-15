package org.datepollsystems.waiterrobot.shared.features.table.data

import org.datepollsystems.waiterrobot.shared.core.repository.AbstractRepository
import org.datepollsystems.waiterrobot.shared.features.billing.api.BillingApi
import org.datepollsystems.waiterrobot.shared.features.table.data.local.TableDatabase
import org.datepollsystems.waiterrobot.shared.features.table.data.remote.TableApi
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.OrderedItem
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableRepository
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable

internal class TableRepositoryImpl(
    private val tableApi: TableApi,
    private val billingApi: BillingApi,
    private val tableDatabase: TableDatabase
) : TableRepository, AbstractRepository() {
    override suspend fun updateTablesWithOpenOrders(eventId: Long) {
        val ids = tableApi.getUnpaidTableIds(eventId)
        tableDatabase.updateTablesWithOrder(ids)
    }

    override suspend fun getUnpaidOrderItems(
        table: Table
    ): Result<List<OrderedItem>> = runCatchingCancelable {
        billingApi.getBillForTable(table.id).getBillItems(false).map {
            OrderedItem(
                baseProductId = it.baseProductId,
                name = it.name,
                amount = it.ordered,
                virtualId = it.orderProductIds.first(),
                note = "" // TODO propagate note
            )
        }
    }
}
