package org.datepollsystems.waiterrobot.android.ui.tabledetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import org.datepollsystems.waiterrobot.android.ui.common.CenteredText
import org.datepollsystems.waiterrobot.android.ui.core.ErrorBar
import org.datepollsystems.waiterrobot.android.ui.core.LocalSnackbarHostState
import org.datepollsystems.waiterrobot.android.ui.core.handleSideEffects
import org.datepollsystems.waiterrobot.android.ui.core.view.LoadingView
import org.datepollsystems.waiterrobot.android.ui.core.view.RefreshableView
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.OrderedItem
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.features.table.presentation.detail.TableDetailViewModel
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.newOrder
import org.datepollsystems.waiterrobot.shared.generated.localization.noOrder
import org.datepollsystems.waiterrobot.shared.generated.localization.title
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState

@Composable
@Destination
fun TableDetailScreen(
    table: Table,
    vm: TableDetailViewModel = koinViewModel { parametersOf(table) },
    navigator: NavController,
) {
    val state by vm.collectAsState()

    vm.handleSideEffects(navigator)

    Scaffold(
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        topBar = {
            TopAppBar(
                title = { Text(L.tableDetail.title(table.groupName, table.number.toString())) },
                navigationIcon = {
                    IconButton(onClick = navigator::popBackStack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (!state.orderedItems.data.isNullOrEmpty()) {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = vm::openBillingScreen
                    ) {
                        Icon(Icons.Filled.CreditCard, contentDescription = "Pay")
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                }
                ExtendedFloatingActionButton(
                    onClick = vm::openOrderScreen,
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = L.tableDetail.newOrder()
                        )
                    },
                    text = { Text(L.tableDetail.newOrder()) }
                )
            }
        }
    ) {
        RefreshableView(
            modifier = Modifier.padding(it),
            loading = state.orderedItems is Resource.Loading && state.orderedItems.data != null,
            onRefresh = vm::refreshOrder,
        ) {
            if (state.orderedItems is Resource.Loading && state.orderedItems.data == null) {
                LoadingView()
            } else {
                Column {
                    val res = state.orderedItems
                    val orderedItems = state.orderedItems.data

                    if (res is Resource.Error) {
                        ErrorBar(message = res.userMessage, retryAction = vm::refreshOrder)
                    }

                    if (orderedItems.isNullOrEmpty()) {
                        CenteredText(
                            modifier = Modifier.weight(1f),
                            text = L.tableDetail.noOrder(table.groupName, table.number.toString()),
                            scrollAble = true
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(orderedItems, key = OrderedItem::virtualId) { item ->
                                OrderedItem(item = item) {
                                    vm.openOrderScreen(item.baseProductId)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
