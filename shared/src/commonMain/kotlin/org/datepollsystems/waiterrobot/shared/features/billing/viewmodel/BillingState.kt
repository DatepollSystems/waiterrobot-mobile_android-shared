package org.datepollsystems.waiterrobot.shared.features.billing.viewmodel

import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.data.asListResource
import org.datepollsystems.waiterrobot.shared.core.data.objCArray
import org.datepollsystems.waiterrobot.shared.core.viewmodel.DialogState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.features.billing.models.BillItem
import org.datepollsystems.waiterrobot.shared.features.billing.viewmodel.ChangeBreakUp.Companion.breakUp
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event
import org.datepollsystems.waiterrobot.shared.utils.Money
import org.datepollsystems.waiterrobot.shared.utils.cent
import org.datepollsystems.waiterrobot.shared.utils.euro
import org.datepollsystems.waiterrobot.shared.utils.sumOf
import kotlin.math.abs
import kotlin.native.ObjCName

data class BillingState(
    val moneyGivenText: String = "",
    val change: Change? = null,
    val paymentState: PaymentState? = null,
    @Suppress("PropertyName", "ConstructorParameterNaming")
    internal val _billItems: Resource<Map<Long, BillItem>> = Resource.Loading(),
) : ViewModelState {

    val billItems: Resource<List<BillItem>> by _billItems.asListResource()

    @Suppress("unused") // iOS only
    @ObjCName("billItems")
    val billItemsArray: Resource<Array<BillItem>> by billItems.objCArray()

    val priceSum: Money by lazy {
        _billItems.data?.values?.sumOf { it.pricePerPiece * it.ordered } ?: Money(0)
    }

    val hasSelectedItems: Boolean by lazy {
        _billItems.data?.values?.any { it.selectedForBill > 0 } ?: false
    }

    val hasCustomSelection: Boolean by lazy {
        _billItems.data?.values?.any { it.ordered > 0 && it.ordered != it.selectedForBill } ?: false
    }

    val contactLessState: ContactLessState by lazy {
        if (CommonApp.stripeProvider?.connectedToReader?.value != true) return@lazy ContactLessState.DISABLED

        when (val stripeSettings = CommonApp.settings.selectedEvent?.stripeSettings) {
            null, Event.StripeSettings.Disabled -> ContactLessState.DISABLED
            is Event.StripeSettings.Enabled -> {
                if (stripeSettings.minAmount > priceSum.cents) {
                    ContactLessState.AMOUNT_TOO_LOW
                } else {
                    ContactLessState.ENABLED
                }
            }
        }
    }

    data class Change(
        val amount: Money,
        val breakUp: List<ChangeBreakUp> = amount.breakUp(),
        val brokenDown: Boolean = false
    ) {
        @Suppress("unused") // iOS only
        val breakUpArray: Array<ChangeBreakUp> by lazy { breakUp.toTypedArray() }
    }

    enum class ContactLessState {
        DISABLED, ENABLED, AMOUNT_TOO_LOW
    }

    sealed class PaymentState {
        object Loading : PaymentState()
        object Success : PaymentState()
        data class Error(val dialog: DialogState) : PaymentState()
    }
}

data class ChangeBreakUp(
    val quantity: Int,
    val amount: Money
) {
    companion object {
        private val amountOrder = listOf(
            100.euro, 50.euro, 20.euro, 10.euro, 5.euro, 2.euro, 1.euro,
            50.cent, 20.cent, 10.cent, 5.cent, 2.cent, 1.cent
        )

        internal fun Money.breakUp(exclude: Money? = null): List<ChangeBreakUp> = buildList {
            amountOrder.filter { it != exclude }
                .fold(abs(this@breakUp.cents)) { leftOverCents, currentAmount ->
                    val quantity = leftOverCents / currentAmount.cents
                    if (quantity > 0) add(ChangeBreakUp(quantity, currentAmount))
                    leftOverCents - quantity * currentAmount.cents
                }
        }

        internal fun List<ChangeBreakUp>.breakDown(changeBreakUp: ChangeBreakUp): List<ChangeBreakUp> {
            if (changeBreakUp.amount <= amountOrder.last()) return this // Can not be further broken down

            return buildList {
                addAll(this@breakDown.filter { it.amount != changeBreakUp.amount })
                add(changeBreakUp.copy(quantity = changeBreakUp.quantity - 1))
                addAll(changeBreakUp.amount.breakUp(exclude = changeBreakUp.amount))
            }.groupBy { it.amount }
                .values
                .map { quantityList ->
                    quantityList.first().copy(
                        quantity = quantityList.sumOf(ChangeBreakUp::quantity)
                    )
                }
                .filter { it.quantity > 0 }
                .sortedByDescending { it.amount }
        }
    }
}
