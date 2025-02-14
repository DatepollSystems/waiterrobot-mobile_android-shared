package org.datepollsystems.waiterrobot.shared.features.table.presentation.list

import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.data.objCArray
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.GroupedTables
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

data class TableListState(
    @HiddenFromObjC
    val tableGroups: Resource<List<GroupedTables>> = Resource.Loading(),
    val hasHiddenGroups: Boolean = false
) : ViewModelState {

    @Suppress("unused") // iOS only
    @ObjCName("tableGroups")
    val tableGroupsArray by tableGroups.objCArray()
}
