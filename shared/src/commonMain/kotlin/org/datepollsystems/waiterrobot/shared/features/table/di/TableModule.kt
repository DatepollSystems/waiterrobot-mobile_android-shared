package org.datepollsystems.waiterrobot.shared.features.table.di

import org.datepollsystems.waiterrobot.shared.core.di.getApiClient
import org.datepollsystems.waiterrobot.shared.core.di.sharedViewModel
import org.datepollsystems.waiterrobot.shared.features.billing.api.BillingApi
import org.datepollsystems.waiterrobot.shared.features.table.api.TableApi
import org.datepollsystems.waiterrobot.shared.features.table.repository.TableRepository
import org.datepollsystems.waiterrobot.shared.features.table.viewmodel.detail.TableDetailViewModel
import org.datepollsystems.waiterrobot.shared.features.table.viewmodel.list.TableListViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

internal val tableModule: Module = module {
    single { TableRepository(get(), get()) }
    single { TableApi(getApiClient()) }
    single { BillingApi(getApiClient()) }
    sharedViewModel { TableListViewModel(get()) }
    sharedViewModel { params -> TableDetailViewModel(get(), params.get()) }
}
