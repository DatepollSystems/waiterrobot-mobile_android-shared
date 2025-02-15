package org.datepollsystems.waiterrobot.shared.features.table.presentation.list

import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.navigation.Screen
import org.datepollsystems.waiterrobot.shared.core.viewmodel.AbstractViewModel
import org.datepollsystems.waiterrobot.shared.features.table.domain.GetGroupedTablesUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.HasHiddenGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.RefreshTableGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.UpdateTablesWithOpenOrdersUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.utils.repeatUntilCanceled
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.syntax.simple.subIntent
import kotlin.time.Duration.Companion.minutes

class TableListViewModel internal constructor(
    private val getGroupedTablesUseCase: GetGroupedTablesUseCase,
    private val hasHiddenGroupsUseCase: HasHiddenGroupsUseCase,
    private val refreshTableGroupsUseCase: RefreshTableGroupsUseCase,
    private val updateTablesWithOpenOrdersUseCase: UpdateTablesWithOpenOrdersUseCase,
) : AbstractViewModel<TableListState, TableListEffect>(TableListState()) {

    override suspend fun onCreate() = subIntent {
        repeatOnSubscription {
            launch { refreshTablesInternal() }
            launch {
                getGroupedTablesUseCase().collect {
                    reduce { state.copy(tableGroups = it) }
                }
            }
            launch {
                hasHiddenGroupsUseCase().collect {
                    reduce { state.copy(hasHiddenGroups = it) }
                }
            }
            launch {
                repeatUntilCanceled(1.minutes) {
                    updateTablesWithOpenOrdersUseCase().onFailure { e ->
                        logger.i("Refreshing of tables with open orders failed", e)
                    }
                }
            }
        }
    }

    override suspend fun onUnhandledException(exception: Throwable) {
        TODO("Not yet implemented")
    }

    fun refreshTables() = intent {
        refreshTablesInternal()
    }

    private suspend fun refreshTablesInternal() = subIntent {
        reduce { state.copy(tableGroups = Resource.Loading(state.tableGroups.data)) }
        refreshTableGroupsUseCase().onFailure { exception ->
            reduce { state.copy(tableGroups = Resource.Error(exception, state.tableGroups.data)) }
        }
    }

    fun onTableClick(table: Table) = intent {
        navigator.push(Screen.TableDetailScreen(table))
    }

    fun openSettings() = intent {
        navigator.push(Screen.SettingsScreen)
    }
}
