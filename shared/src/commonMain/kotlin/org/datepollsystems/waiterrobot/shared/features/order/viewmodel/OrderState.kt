package org.datepollsystems.waiterrobot.shared.features.order.viewmodel

import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.OrderItem
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

data class OrderState(
    val orderingState: ViewState = ViewState.Idle,
    @Suppress("ConstructorParameterNaming", "PropertyName")
    internal val _currentOrder: Map<Long, OrderItem> = emptyMap(), // Product ID to Order
) : ViewModelState {

    // Expose only as a list of OrderItems
    @HiddenFromObjC
    val currentOrder: List<OrderItem> by lazy { _currentOrder.values.toList() }

    @Suppress("unused") // iOS only
    @ObjCName("currentOrder")
    val currentOrderArray: Array<OrderItem> by lazy { currentOrder.toTypedArray() }

    @Suppress("unused") // iOS only
    val hasSelectedItems: Boolean by lazy { !currentOrder.isEmpty() }
}
