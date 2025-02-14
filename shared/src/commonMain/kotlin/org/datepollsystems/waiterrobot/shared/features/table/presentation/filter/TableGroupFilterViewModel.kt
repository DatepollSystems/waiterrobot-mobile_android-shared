package org.datepollsystems.waiterrobot.shared.features.table.presentation.filter

import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.shared.core.viewmodel.AbstractViewModel
import org.datepollsystems.waiterrobot.shared.features.table.domain.GetTableGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.HideTableGroupUseCases
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.TableGroup
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.syntax.simple.subIntent

class TableGroupFilterViewModel internal constructor(
    private val getTableGroupsUseCase: GetTableGroupsUseCase,
    private val hideTableGroupUseCases: HideTableGroupUseCases,
) : AbstractViewModel<TableGroupFilterState, TableGroupFilterEffect>(TableGroupFilterState()) {

    override suspend fun onCreate() = subIntent {
        repeatOnSubscription {
            launch {
                getTableGroupsUseCase().collect {
                    reduce { state.copy(groups = it) }
                }
            }
        }
    }

    fun toggleFilter(tableGroup: TableGroup) = intent {
        hideTableGroupUseCases.toggle(tableGroup.id)
    }

    fun showAll() = intent {
        hideTableGroupUseCases.showAll()
    }

    fun hideAll() = intent {
        hideTableGroupUseCases.hideAll()
    }

    override suspend fun onUnhandledException(exception: Throwable) {
        TODO("Not yet implemented")
    }
}
