package org.datepollsystems.waiterrobot.shared.features.settings.di

import org.datepollsystems.waiterrobot.shared.core.di.sharedViewModelOf
import org.datepollsystems.waiterrobot.shared.features.order.orderModule
import org.datepollsystems.waiterrobot.shared.features.settings.viewmodel.SettingsViewModel
import org.datepollsystems.waiterrobot.shared.features.table.tableModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal val settingsModule: Module = module {
    includes(tableModule)
    includes(orderModule)

    sharedViewModelOf(::SettingsViewModel)
}
