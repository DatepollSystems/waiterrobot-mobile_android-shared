package org.datepollsystems.waiterrobot.shared.features.billing.di

import org.datepollsystems.waiterrobot.shared.core.di.sharedViewModelOf
import org.datepollsystems.waiterrobot.shared.features.billing.api.BillingApi
import org.datepollsystems.waiterrobot.shared.features.billing.repository.BillingRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.billing.viewmodel.BillingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val billingModule: Module = module {
    singleOf(::BillingApi)
    singleOf(::BillingRepositoryImpl)
    sharedViewModelOf(::BillingViewModel)
}
