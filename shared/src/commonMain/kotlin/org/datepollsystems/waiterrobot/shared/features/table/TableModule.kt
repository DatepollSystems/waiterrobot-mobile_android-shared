package org.datepollsystems.waiterrobot.shared.features.table

import org.datepollsystems.waiterrobot.shared.core.di.sharedViewModelOf
import org.datepollsystems.waiterrobot.shared.features.billing.di.billingModule
import org.datepollsystems.waiterrobot.shared.features.table.data.TableGroupRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.table.data.TableRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.table.data.local.TableDatabase
import org.datepollsystems.waiterrobot.shared.features.table.data.remote.TableApi
import org.datepollsystems.waiterrobot.shared.features.table.domain.GetGroupedTablesUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.GetTableGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.HasHiddenGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.HideTableGroupUseCases
import org.datepollsystems.waiterrobot.shared.features.table.domain.RefreshTableGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.UpdateTablesWithOpenOrdersUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableGroupRepository
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableRepository
import org.datepollsystems.waiterrobot.shared.features.table.presentation.detail.TableDetailViewModel
import org.datepollsystems.waiterrobot.shared.features.table.presentation.filter.TableGroupFilterViewModel
import org.datepollsystems.waiterrobot.shared.features.table.presentation.list.TableListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal val tableModule: Module = module {
    includes(billingModule)

    singleOf(::TableDatabase)
    singleOf(::TableApi)

    singleOf(::TableRepositoryImpl).bind<TableRepository>()
    singleOf(::TableGroupRepositoryImpl).bind<TableGroupRepository>()

    singleOf(::GetGroupedTablesUseCase)
    singleOf(::HasHiddenGroupsUseCase)
    singleOf(::RefreshTableGroupsUseCase)
    singleOf(::UpdateTablesWithOpenOrdersUseCase)
    singleOf(::GetTableGroupsUseCase)
    singleOf(::HideTableGroupUseCases)

    sharedViewModelOf(::TableListViewModel)
    sharedViewModelOf(::TableDetailViewModel)
    sharedViewModelOf(::TableGroupFilterViewModel)
}
