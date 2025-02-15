package org.datepollsystems.waiterrobot.shared.features.billing.viewmodel

import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.data.api.ApiException
import org.datepollsystems.waiterrobot.shared.core.viewmodel.AbstractViewModel
import org.datepollsystems.waiterrobot.shared.core.viewmodel.DialogState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState
import org.datepollsystems.waiterrobot.shared.features.billing.api.models.PayBillRequestDto
import org.datepollsystems.waiterrobot.shared.features.billing.models.BillItem
import org.datepollsystems.waiterrobot.shared.features.billing.repository.BillingRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.billing.repository.GeoLocationDisabledException
import org.datepollsystems.waiterrobot.shared.features.billing.repository.NfcDisabledException
import org.datepollsystems.waiterrobot.shared.features.billing.repository.PaymentCanceledException
import org.datepollsystems.waiterrobot.shared.features.billing.repository.StripeProvider
import org.datepollsystems.waiterrobot.shared.features.billing.viewmodel.ChangeBreakUp.Companion.breakDown
import org.datepollsystems.waiterrobot.shared.features.stripe.api.StripeApi
import org.datepollsystems.waiterrobot.shared.features.stripe.api.models.PaymentIntent
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.desc
import org.datepollsystems.waiterrobot.shared.generated.localization.locationDisabled
import org.datepollsystems.waiterrobot.shared.generated.localization.nfcDisabled
import org.datepollsystems.waiterrobot.shared.generated.localization.success
import org.datepollsystems.waiterrobot.shared.generated.localization.title
import org.datepollsystems.waiterrobot.shared.utils.euro
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable
import org.datepollsystems.waiterrobot.shared.utils.getLocalizedUserMessage
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.syntax.simple.subIntent

@Suppress("TooManyFunctions")
class BillingViewModel internal constructor(
    private val billingRepository: BillingRepositoryImpl,
    private val stripeApi: StripeApi,
    private val table: Table
) : AbstractViewModel<BillingState, BillingEffect>(BillingState()) {

    override suspend fun onCreate() = subIntent {
        repeatOnSubscription {
            launch { refreshBillInternal() }
        }
    }

    override suspend fun onUnhandledException(exception: Throwable) {
        TODO("Not yet implemented")
    }

    fun refreshBill() = intent { refreshBillInternal() }

    private suspend fun refreshBillInternal() = subIntent {
        reduce { state.copy(_billItems = state._billItems.loading()) }
        billingRepository.getBillForTable(
            table,
            CommonApp.settings.paymentSelectAllProductsByDefault
        ).onSuccess {
            reduce { state.copy(_billItems = Resource.Success(it), paymentState = ViewState.Idle) }
        }.onFailure { e ->
            reduce {
                state.copy(
                    _billItems = Resource.Error(e, state._billItems.data),
                    paymentState = ViewState.Idle
                )
            }
        }
    }

    fun paySelection(paymentSheetShown: Boolean = false) = intent {
        if (!CommonApp.settings.skipMoneyBackDialog && !paymentSheetShown) {
            postSideEffect(BillingEffect.ShowPaymentSheet)
            return@intent
        }

        reduce { state.copy(paymentState = ViewState.Loading) }

        val selectedItems = state.billItems.data?.filter { it.selectedForBill > 0 } ?: emptyList()
        billingRepository.payBill(
            table = table,
            items = selectedItems,
            selectAll = CommonApp.settings.paymentSelectAllProductsByDefault
        ).onSuccess {
            reduce {
                state.copy(
                    _billItems = Resource.Success(it),
                    change = null,
                    paymentState = ViewState.Idle
                )
            }
        }.onFailure { e ->
            when (e) {
                is ApiException.BillProductsAlreadyPayed -> {
                    logger.i("Some products have already been payed.")
                    reduce {
                        state.copy(
                            paymentState = ViewState.ErrorDialog(
                                DialogState(
                                    L.billing.productsAlreadyPayed.title(),
                                    L.billing.productsAlreadyPayed.desc(),
                                    primaryButton = DialogState.Button("Refresh", ::refreshBill),
                                    onDismiss = ::dismissPaymentState
                                )
                            )
                        )
                    }
                }

                else -> {
                    logger.e("Failed to pay bill", e)
                    reduce {
                        state.copy(
                            paymentState = ViewState.ErrorDialog(
                                DialogState(
                                    title = L.exceptions.title(),
                                    text = e.getLocalizedUserMessage(),
                                    primaryButton = DialogState.Button("Refresh", ::refreshBill),
                                    onDismiss = ::dismissPaymentState
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private fun dismissPaymentState() = intent {
        reduce { state.copy(paymentState = ViewState.Idle) }
    }

    fun initiateContactLessPayment() = intent {
        val stripeProvider = CommonApp.stripeProvider
        if (stripeProvider == null) {
            logger.e("Tried to initiate contactless payment but no stripe provider was set.")
            return@intent
        }

        if (!stripeProvider.connectedToReader.value) {
            logger.e("Tried to initiate contactless payment but no reader was connected.")
            return@intent
        }

        reduce { state.copy(paymentState = ViewState.Loading) }

        val items = state._billItems.data?.values?.flatMap {
            it.orderProductIds.take(it.selectedForBill)
        } ?: emptyList()

        runCatchingCancelable {
            stripeApi.createPaymentIntent(PayBillRequestDto(table.id, items))
        }.onSuccess {
            collectPayment(stripeProvider, it)
        }.onFailure { e ->
            logger.e(e) { "Failed to initiate contactless payment" }
            reduce {
                state.copy(
                    paymentState = ViewState.ErrorDialog(
                        DialogState(
                            title = L.exceptions.title(),
                            text = e.getLocalizedUserMessage(),
                            primaryButton = DialogState.Button("Refresh", ::refreshBill),
                            onDismiss = ::dismissPaymentState
                        )
                    )
                )
            }
        }
    }

    fun moneyGiven(moneyGiven: String) = blockingIntent {
        if (!moneyGiven.matches(Regex("""^(\d+([.,]\d{0,2})?)?$"""))) return@blockingIntent
        val givenText = moneyGiven.replace(",", ".")
        reduce {
            try {
                val given = givenText.euro
                state.copy(
                    moneyGivenText = givenText,
                    change = BillingState.Change(amount = given - state.priceSum),
                )
            } catch (_: Exception) {
                state.copy(
                    moneyGivenText = givenText,
                    change = null,
                )
            }
        }
    }

    fun breakDownChange(changeBreakUp: ChangeBreakUp) = intent {
        reduce {
            val change = state.change
            state.copy(
                change = change?.copy(
                    breakUp = change.breakUp.breakDown(changeBreakUp),
                    brokenDown = true
                )
            )
        }
    }

    fun resetChange() = intent {
        reduce {
            state.copy(
                change = state.change?.amount?.let { BillingState.Change(it) }
            )
        }
    }

    fun addItem(baseProductId: Long, amount: Int) = intent {
        reduce {
            val item = state._billItems.data?.get(baseProductId)

            if (item == null) {
                logger.e("Tried to add product with id '$baseProductId' but could not find the product on the bill.")
                return@reduce state
            }

            val newAmount = (item.selectedForBill + amount).coerceIn(0..item.ordered)

            val newItem = item.copy(selectedForBill = newAmount)
            val newBill = state._billItems.data!!.plus(newItem.baseProductId to newItem)
            state.copy(
                _billItems = state._billItems.internalCopy(newBill),
                moneyGivenText = "",
                change = null
            )
        }
    }

    fun selectAll() = intent {
        reduce {
            val data = state._billItems.data ?: return@reduce state
            val newBill = data.mapValues {
                it.value.copy(selectedForBill = it.value.ordered)
            }
            state.copy(
                _billItems = state._billItems.internalCopy(newBill),
                moneyGivenText = "",
                change = null
            )
        }
    }

    fun unselectAll() = intent {
        reduce {
            val data = state._billItems.data ?: return@reduce state
            val newBill = data.mapValues { it.value.copy(selectedForBill = 0) }
            state.copy(
                _billItems = state._billItems.internalCopy(newBill),
                moneyGivenText = "",
                change = null
            )
        }
    }

    fun abortBill() = intent {
        navigator.pop()
    }

    private suspend fun collectPayment(
        stripeProvider: StripeProvider,
        paymentIntent: PaymentIntent
    ) {
        fun Throwable.getDialogTitle(): String = when (this) {
            is PaymentCanceledException -> L.billing.stripe.canceled.title()
            else -> L.billing.stripe.failed.title()
        }

        fun Throwable.getDialogText(): String = when (this) {
            is PaymentCanceledException -> L.billing.stripe.canceled.desc()
            is GeoLocationDisabledException -> L.billing.stripe.locationDisabled()
            is NfcDisabledException -> L.billing.stripe.nfcDisabled()
            else -> L.billing.stripe.failed.desc()
        }

        subIntent {
            reduce { state.copy(paymentState = ViewState.Loading) }

            runCatchingCancelable {
                stripeProvider.collectPayment(paymentIntent)
            }.onFailure { error ->
                when (error) {
                    is PaymentCanceledException,
                    is GeoLocationDisabledException,
                    is NfcDisabledException -> {
                        logger.i("Card payment (${paymentIntent.id}) failed", error)
                    }

                    else -> logger.e("Failed to initiate payment (${paymentIntent.id})", error)
                }

                reduce {
                    state.copy(
                        paymentState = ViewState.ErrorDialog(
                            DialogState(
                                title = error.getDialogTitle(),
                                text = error.getDialogText(),
                                onDismiss = {
                                    cancelPayment(paymentIntent)
                                },
                                primaryButton = DialogState.Button("OK") {
                                    cancelPayment(paymentIntent)
                                },
                                secondaryButton = DialogState.Button("Retry") {
                                    logger.i("Retrying card payment (${paymentIntent.id})")
                                    intent { collectPayment(stripeProvider, paymentIntent) }
                                }
                            )
                        )
                    )
                }
            }.onSuccess {
                postSideEffect(BillingEffect.Toast(L.billing.stripe.success()))
                refreshBillInternal()
            }
        }
    }

    private fun cancelPayment(paymentIntent: PaymentIntent) = intent {
        reduce {
            state.copy(
                _billItems = state._billItems.loading()
            )
        }

        runCatchingCancelable {
            stripeApi.cancelPaymentIntent(paymentIntent)
                .getBillItems(selectAllForBill = CommonApp.settings.paymentSelectAllProductsByDefault)
                .associateBy(BillItem::baseProductId)
        }.onSuccess {
            reduce {
                state.copy(
                    _billItems = Resource.Success(it),
                    change = null,
                    moneyGivenText = ""
                )
            }
        }.onFailure { e ->
            logger.e("Failed to cancel payment", e)
            reduce {
                state.copy(
                    paymentState = ViewState.ErrorDialog(
                        DialogState(
                            title = L.exceptions.title(),
                            text = e.getLocalizedUserMessage(),
                            primaryButton = DialogState.Button("Refresh", ::refreshBill),
                            onDismiss = ::dismissPaymentState
                        )
                    )
                )
            }
        }
    }
}
