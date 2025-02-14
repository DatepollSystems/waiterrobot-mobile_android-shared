package org.datepollsystems.waiterrobot.shared.features.table.presentation.filter

import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.TableGroup

data class TableGroupFilterState(
    val groups: Resource<List<TableGroup>> = Resource.Loading()
) : ViewModelState
