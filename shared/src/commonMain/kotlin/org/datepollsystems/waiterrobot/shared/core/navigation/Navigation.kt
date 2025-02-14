package org.datepollsystems.waiterrobot.shared.core.navigation

import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelEffect
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.utils.DeepLink

sealed class Screen {
    data object UpdateApp : Screen()
    data object LoginScreen : Screen()
    data object LoginScannerScreen : Screen()
    data object SwitchEventScreen : Screen()
    data object StripeInitializationScreen : Screen()
    data object SettingsScreen : Screen()
    data object TableListScreen : Screen()

    data class RegisterScreen(val registerLink: DeepLink.Auth.RegisterLink) : Screen()
    data class TableDetailScreen(val table: Table) : Screen()
    data class OrderScreen(val table: Table, val initialItemId: Long?) : Screen()
    data class BillingScreen(val table: Table) : Screen()
}

sealed class NavAction {
    data object Pop : NavAction()
    data class Push(val screen: Screen) : NavAction()
    data class PopUpTo(val screen: Screen, val inclusive: Boolean) : NavAction()
    data class ReplaceRoot(val screen: Screen) : NavAction()
    data class PopUpAndPush(val screen: Screen, val popUpTo: Screen, val inclusive: Boolean) :
        NavAction()
}

sealed class NavOrViewModelEffect<out E : ViewModelEffect> {
    class NavEffect<E : ViewModelEffect>(val action: NavAction) : NavOrViewModelEffect<E>()
    class VMEffect<E : ViewModelEffect>(val effect: E) : NavOrViewModelEffect<E>()
}
