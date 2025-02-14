package org.datepollsystems.waiterrobot.shared.features.order.viewmodel

import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.data.objCArray
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.OrderItem
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.ProductGroup
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

data class OrderState(
    @HiddenFromObjC
    val productGroups: Resource<List<ProductGroup>> = Resource.Loading(),
    val orderingState: ViewState = ViewState.Idle,
    @Suppress("ConstructorParameterNaming", "PropertyName")
    internal val _currentOrder: Map<Long, OrderItem> = emptyMap(), // Product ID to Order
    internal val filter: String = "",
) : ViewModelState {

    // Expose only as a list of OrderItems
    @HiddenFromObjC
    val currentOrder: List<OrderItem> by lazy { _currentOrder.values.toList() }

    @Suppress("unused") // iOS only
    @ObjCName("currentOrder")
    val currentOrderArray: Array<OrderItem> by lazy { currentOrder.toTypedArray() }

    @Suppress("unused") // iOS only
    @ObjCName("productGroups")
    val productGroupsArray: Resource<Array<ProductGroup>> by productGroups.objCArray()

    @Suppress("unused") // iOS only
    val hasSelectedItems: Boolean by lazy { !currentOrder.isEmpty() }
}
