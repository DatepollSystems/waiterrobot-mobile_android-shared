package org.datepollsystems.waiterrobot.shared.features.table.presentation.detail

import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.data.objCArray
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.OrderedItem
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

data class TableDetailState(
    @HiddenFromObjC
    val orderedItems: Resource<List<OrderedItem>> = Resource.Loading()
) : ViewModelState {

    @Suppress("unused") // iOS only
    @ObjCName("orderedItems")
    val orderedItemsArray by orderedItems.objCArray()
}
