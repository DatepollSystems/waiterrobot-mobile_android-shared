package org.datepollsystems.waiterrobot.shared.features.order

import org.datepollsystems.waiterrobot.shared.core.di.sharedViewModel
import org.datepollsystems.waiterrobot.shared.features.order.data.OrderRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.order.data.ProductGroupRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.order.data.ProductRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.order.data.local.ProductDatabase
import org.datepollsystems.waiterrobot.shared.features.order.data.remote.OrderApi
import org.datepollsystems.waiterrobot.shared.features.order.data.remote.ProductApi
import org.datepollsystems.waiterrobot.shared.features.order.domain.GetProductGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.order.domain.GetProductUseCase
import org.datepollsystems.waiterrobot.shared.features.order.domain.RefreshProductGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.OrderRepository
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.ProductGroupRepository
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.ProductRepository
import org.datepollsystems.waiterrobot.shared.features.order.viewmodel.OrderViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val orderModule: Module = module {
    singleOf(::OrderApi)
    singleOf(::ProductApi)
    singleOf(::ProductDatabase)

    singleOf(::ProductRepositoryImpl).bind<ProductRepository>()
    singleOf(::ProductGroupRepositoryImpl).bind<ProductGroupRepository>()
    singleOf(::OrderRepositoryImpl).bind<OrderRepository>()

    singleOf(::GetProductUseCase)
    singleOf(::GetProductGroupsUseCase)
    singleOf(::RefreshProductGroupsUseCase)

    // nullable parameters currently are not supported for the constructor dsl
    sharedViewModel { params ->
        OrderViewModel(
            getProductGroupsUseCase = get(),
            getProductUseCase = get(),
            refreshProductGroupsUseCase = get(),
            orderRepository = get(),
            table = params.get(),
            initialItemId = params.getOrNull()
        )
    }
}
