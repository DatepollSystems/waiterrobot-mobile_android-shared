package org.datepollsystems.waiterrobot.shared.features.order.viewmodel

import org.datepollsystems.waiterrobot.shared.core.viewmodel.DialogState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelEffect

sealed class OrderEffect : ViewModelEffect {
    object OrderSending : OrderEffect()
    class OrderError(val dialog: DialogState) : OrderEffect()
}
