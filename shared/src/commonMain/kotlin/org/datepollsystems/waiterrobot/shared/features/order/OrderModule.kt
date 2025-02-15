package org.datepollsystems.waiterrobot.shared.features.order

import org.datepollsystems.waiterrobot.shared.core.di.sharedViewModel
import org.datepollsystems.waiterrobot.shared.features.order.data.OrderRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.order.data.remote.OrderApi
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.OrderRepository
import org.datepollsystems.waiterrobot.shared.features.order.viewmodel.OrderViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val orderModule: Module = module {
    singleOf(::OrderApi)

    singleOf(::OrderRepositoryImpl).bind<OrderRepository>()

    // nullable parameters currently are not supported for the constructor dsl
    sharedViewModel { params ->
        OrderViewModel(
            getProductUseCase = get(),
            refreshProductGroupsUseCase = get(),
            orderRepository = get(),
            table = params.get(),
            initialItemId = params.getOrNull()
        )
    }
}
