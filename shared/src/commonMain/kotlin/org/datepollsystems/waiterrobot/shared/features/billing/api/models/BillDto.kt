package org.datepollsystems.waiterrobot.shared.features.billing.api.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.shared.core.data.api.RequestBodyDto
import org.datepollsystems.waiterrobot.shared.features.billing.models.BillItem
import org.datepollsystems.waiterrobot.shared.utils.Cents
import org.datepollsystems.waiterrobot.shared.utils.cent

@Serializable
internal class BillResponseDto(
    val implodedOrderProducts: List<BillItemDto>,
    val priceSum: Cents,
) {
    @Serializable
    class BillItemDto(
        val name: String,
        val priceSum: Cents,
        val pricePerPiece: Cents,
        val amount: Int,
        val baseProductId: Long,
        val orderProductIds: List<Long>
    )

    fun getBillItems(selectAllForBill: Boolean): List<BillItem> {
        return implodedOrderProducts.fold(mapOf<Long, BillItem>()) { map, billItem ->
            // Safeguard
            if (billItem.orderProductIds.isEmpty()) return@fold map

            val existingBillItem = map[billItem.baseProductId]
            val newItem = existingBillItem?.copy(
                ordered = existingBillItem.ordered + billItem.amount,
                orderProductIds = existingBillItem.orderProductIds + billItem.orderProductIds,
                selectedForBill = existingBillItem.selectedForBill + if (selectAllForBill) billItem.amount else 0,
            ) ?: BillItem(
                baseProductId = billItem.baseProductId,
                name = billItem.name,
                ordered = billItem.amount,
                selectedForBill = if (selectAllForBill) billItem.amount else 0,
                pricePerPiece = billItem.pricePerPiece.cent,
                orderProductIds = billItem.orderProductIds,
            )

            map + (billItem.baseProductId to newItem)
        }.values.toList()
    }
}

@Serializable
internal class PayBillRequestDto(
    val tableId: Long,
    val orderProducts: List<Long>,
    val unpaidReasonId: Long? = null,
) : RequestBodyDto

@Serializable
internal class PayBillResponseDto(
    val bill: Bill,
    val openBill: BillResponseDto,
) {
    @Serializable
    class Bill(
        val id: Long,
        val waiter: WaiterDto,
        val createdAt: Instant,
        val pricePaidSum: Int,
        val unpaidReason: UnpaidReasonDto?,
        val implodedBillProducts: List<BillItemDto>,
    ) {
        @Serializable
        class WaiterDto(
            val id: Long,
            val name: String,
        )

        @Serializable
        class UnpaidReasonDto(
            val id: Long,
            val reason: String,
            val description: String?,
            val isGlobal: Boolean,
        )

        @Serializable
        class BillItemDto(
            val name: String,
            val pricePaidSum: Cents,
            val pricePaidPerPiece: Cents,
            val productId: Long,
            val amount: Int,
            val billProductIds: List<Long>,
        )
    }
}
