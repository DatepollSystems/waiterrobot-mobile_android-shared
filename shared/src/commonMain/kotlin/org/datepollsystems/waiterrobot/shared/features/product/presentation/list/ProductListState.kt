package org.datepollsystems.waiterrobot.shared.features.product.presentation.list

import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.data.objCArray
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.GroupedProducts
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

data class ProductListState(
    @HiddenFromObjC
    val productGroups: Resource<List<GroupedProducts>> = Resource.Loading(),
    val filter: String = "",
) : ViewModelState {
    @Suppress("unused") // iOS only
    @ObjCName("productGroups")
    val productGroupsArray: Resource<Array<GroupedProducts>> by productGroups.objCArray()
}
