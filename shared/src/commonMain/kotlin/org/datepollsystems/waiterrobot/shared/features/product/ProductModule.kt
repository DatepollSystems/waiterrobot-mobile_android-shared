package org.datepollsystems.waiterrobot.shared.features.product

import org.datepollsystems.waiterrobot.shared.core.di.sharedViewModelOf
import org.datepollsystems.waiterrobot.shared.features.product.data.ProductGroupRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.product.data.ProductRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.product.data.local.ProductDatabase
import org.datepollsystems.waiterrobot.shared.features.product.data.remote.ProductApi
import org.datepollsystems.waiterrobot.shared.features.product.domain.GetProductGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.product.domain.GetProductUseCase
import org.datepollsystems.waiterrobot.shared.features.product.domain.RefreshProductGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.product.domain.repository.ProductGroupRepository
import org.datepollsystems.waiterrobot.shared.features.product.domain.repository.ProductRepository
import org.datepollsystems.waiterrobot.shared.features.product.presentation.list.ProductListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val productModule: Module = module {
    singleOf(::ProductDatabase)
    singleOf(::ProductApi)

    singleOf(::ProductRepositoryImpl).bind<ProductRepository>()
    singleOf(::ProductGroupRepositoryImpl).bind<ProductGroupRepository>()

    singleOf(::GetProductGroupsUseCase)
    singleOf(::GetProductUseCase)
    singleOf(::RefreshProductGroupsUseCase)

    sharedViewModelOf(::ProductListViewModel)
}
