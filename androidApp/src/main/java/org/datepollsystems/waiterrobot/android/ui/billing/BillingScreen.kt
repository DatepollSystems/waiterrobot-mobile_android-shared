package org.datepollsystems.waiterrobot.android.ui.billing

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.android.ui.core.AlertDialogFromState
import org.datepollsystems.waiterrobot.android.ui.core.ConfirmDialog
import org.datepollsystems.waiterrobot.android.ui.core.handleSideEffects
import org.datepollsystems.waiterrobot.android.ui.core.view.ScaffoldView
import org.datepollsystems.waiterrobot.shared.features.billing.viewmodel.BillingEffect
import org.datepollsystems.waiterrobot.shared.features.billing.viewmodel.BillingState
import org.datepollsystems.waiterrobot.shared.features.billing.viewmodel.BillingViewModel
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.closeAnyway
import org.datepollsystems.waiterrobot.shared.generated.localization.desc
import org.datepollsystems.waiterrobot.shared.generated.localization.keepBill
import org.datepollsystems.waiterrobot.shared.generated.localization.title
import org.datepollsystems.waiterrobot.shared.generated.localization.total
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState

@Composable
@Destination
fun BillingScreen(
    table: Table,
    navigator: NavController,
    vm: BillingViewModel = koinViewModel { parametersOf(table) }
) {
    val state by vm.collectAsState()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequest = remember { FocusRequester() }
    var showConfirmGoBack by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    val paymentSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    vm.handleSideEffects(navigator) {
        when (it) {
            is BillingEffect.Toast -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            BillingEffect.ShowPaymentSheet -> showPaymentSheet = true
        }
    }

    fun goBack() {
        when {
            state.hasCustomSelection -> showConfirmGoBack = true
            else -> vm.abortBill()
        }
    }

    BackHandler(onBack = ::goBack)

    if (showConfirmGoBack) {
        ConfirmDialog(
            title = L.billing.notSent.title(),
            text = L.billing.notSent.desc(),
            confirmText = L.dialog.closeAnyway(),
            onConfirm = vm::abortBill,
            cancelText = L.billing.keepBill(),
            onCancel = { showConfirmGoBack = false },
        )
    }

    when (val paymentState = state.paymentState) {
        is BillingState.PaymentState.Error -> AlertDialogFromState(paymentState.dialog)
        else -> Unit
    }

    ScaffoldView(
        title = L.billing.title(table.groupName, table.number.toString()),
        navigationIcon = {
            IconButton(onClick = ::goBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = vm::selectAll) {
                        Icon(Icons.Filled.DoneAll, contentDescription = "Select all items")
                    }
                    IconButton(onClick = vm::unselectAll) {
                        Icon(Icons.Filled.RemoveDone, contentDescription = "Unselect all items")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = L.billing.total() + ":",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = state.priceSum.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                floatingActionButton = {
                    if (state.hasSelectedItems) {
                        FloatingActionButton(
                            modifier = Modifier.padding(start = 16.dp),
                            onClick = vm::paySelection
                        ) {
                            Icon(Icons.Filled.EuroSymbol, contentDescription = "Pay")
                        }
                    }
                }
            )
        },
        bottomSheet = {
            if (showPaymentSheet) {
                LaunchedEffect(paymentSheetState.isVisible) {
                    if (paymentSheetState.isVisible) focusRequest.requestFocus()
                }

                ModalBottomSheet(
                    onDismissRequest = {
                        focusManager.clearFocus()
                        showPaymentSheet = false
                    },
                    sheetState = paymentSheetState,
                    dragHandle = null
                ) {
                    PaymentView(
                        sum = state.priceSum.toString(),
                        moneyGivenText = state.moneyGivenText,
                        moneyGiven = vm::moneyGiven,
                        moneyGivenInputFocusRequester = focusRequest,
                        change = state.change,
                        breakDownChange = vm::breakDownChange,
                        resetChangeBreakUp = vm::resetChange,
                        contactLessState = state.contactLessState,
                        onContactless = {
                            vm.initiateContactLessPayment()
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                paymentSheetState.hide()
                            }.invokeOnCompletion {
                                if (!paymentSheetState.isVisible) {
                                    showPaymentSheet = false
                                }
                            }
                        },
                        onPayClick = {
                            vm.paySelection(paymentSheetShown = true)
                            focusManager.clearFocus()
                            coroutineScope.launch {
                                paymentSheetState.hide()
                            }.invokeOnCompletion {
                                if (!paymentSheetState.isVisible) {
                                    showPaymentSheet = false
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        // TODO: Add a loading view
        BillList(
            table = table,
            billItems = state.billItems.data ?: emptyList(),
            addAction = vm::addItem
        )
    }
}
