package org.datepollsystems.waiterrobot.shared.features.table.presentation.detail

import co.touchlab.skie.configuration.annotations.DefaultArgumentInterop
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.navigation.Screen
import org.datepollsystems.waiterrobot.shared.core.viewmodel.AbstractViewModel
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableRepository
import org.datepollsystems.waiterrobot.shared.utils.repeatUntilCanceled
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.syntax.simple.subIntent
import kotlin.time.Duration.Companion.minutes

class TableDetailViewModel internal constructor(
    private val tableRepository: TableRepository,
    private val table: Table
) : AbstractViewModel<TableDetailState, TableDetailEffect>(TableDetailState()) {

    override suspend fun onCreate() = subIntent {
        repeatOnSubscription {
            repeatUntilCanceled(1.minutes) { loadOrder() }
        }
    }

    override suspend fun onUnhandledException(exception: Throwable) {
        TODO("Not yet implemented")
    }

    fun refreshOrder() = intent {
        reduce { state.copy(orderedItems = state.orderedItems.loading()) }
        loadOrder()
    }

    private suspend fun loadOrder() = subIntent {
        tableRepository.getUnpaidOrderItems(table)
            .onSuccess { resource ->
                reduce { state.copy(orderedItems = Resource.Success(resource)) }
            }
            .onFailure { error ->
                reduce { state.copy(orderedItems = state.orderedItems.error(error)) }
            }
    }

    @DefaultArgumentInterop.Enabled
    fun openOrderScreen(initialItemId: Long? = null) = intent {
        navigator.push(Screen.OrderScreen(table, initialItemId))
    }

    fun openBillingScreen() = intent {
        navigator.push(Screen.BillingScreen(table))
    }
}
