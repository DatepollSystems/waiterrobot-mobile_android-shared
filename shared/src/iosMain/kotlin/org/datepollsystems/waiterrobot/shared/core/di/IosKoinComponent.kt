package org.datepollsystems.waiterrobot.shared.core.di

import co.touchlab.kermit.Logger
import org.datepollsystems.waiterrobot.shared.features.auth.viewmodel.LoginViewModel
import org.datepollsystems.waiterrobot.shared.features.auth.viewmodel.register.RegisterViewModel
import org.datepollsystems.waiterrobot.shared.features.auth.viewmodel.scanner.LoginScannerViewModel
import org.datepollsystems.waiterrobot.shared.features.billing.viewmodel.BillingViewModel
import org.datepollsystems.waiterrobot.shared.features.order.viewmodel.OrderViewModel
import org.datepollsystems.waiterrobot.shared.features.settings.viewmodel.SettingsViewModel
import org.datepollsystems.waiterrobot.shared.features.switchevent.viewmodel.SwitchEventViewModel
import org.datepollsystems.waiterrobot.shared.features.table.models.Table
import org.datepollsystems.waiterrobot.shared.features.table.viewmodel.detail.TableDetailViewModel
import org.datepollsystems.waiterrobot.shared.features.table.viewmodel.list.TableListViewModel
import org.datepollsystems.waiterrobot.shared.root.RootViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

@Suppress("unused", "TooManyFunctions") // Only used by iOS
object IosKoinComponent : KoinComponent {
    fun logger(tag: String): Logger = get { parametersOf(tag) }
    fun rootVM() = get<RootViewModel>()
    fun loginVM() = get<LoginViewModel>()
    fun registerVM() = get<RegisterViewModel>()
    fun loginScannerVM() = get<LoginScannerViewModel>()
    fun switchEventVM() = get<SwitchEventViewModel>()
    fun tableListVM() = get<TableListViewModel>()
    fun tableDetailVM(table: Table) = get<TableDetailViewModel> { parametersOf(table) }
    fun orderVM(table: Table, initialItemId: Long?) =
        get<OrderViewModel> { parametersOf(table, initialItemId) }

    fun billingVM(table: Table) = get<BillingViewModel> { parametersOf(table) }
    fun settingsVM() = get<SettingsViewModel>()
}
