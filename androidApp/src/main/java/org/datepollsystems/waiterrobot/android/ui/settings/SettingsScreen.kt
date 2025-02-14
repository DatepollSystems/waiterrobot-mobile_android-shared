package org.datepollsystems.waiterrobot.android.ui.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.outlined.Contactless
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import org.datepollsystems.waiterrobot.android.ui.common.SingleSelectDialog
import org.datepollsystems.waiterrobot.android.ui.core.handleSideEffects
import org.datepollsystems.waiterrobot.android.ui.core.view.ScaffoldView
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.features.settings.models.AppTheme
import org.datepollsystems.waiterrobot.shared.features.settings.viewmodel.SettingsEffect
import org.datepollsystems.waiterrobot.shared.features.settings.viewmodel.SettingsViewModel
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.action
import org.datepollsystems.waiterrobot.shared.generated.localization.cancel
import org.datepollsystems.waiterrobot.shared.generated.localization.confirmAction
import org.datepollsystems.waiterrobot.shared.generated.localization.confirmDesc
import org.datepollsystems.waiterrobot.shared.generated.localization.desc
import org.datepollsystems.waiterrobot.shared.generated.localization.keepLoggedIn
import org.datepollsystems.waiterrobot.shared.generated.localization.privacyPolicy
import org.datepollsystems.waiterrobot.shared.generated.localization.title
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
@Destination
fun SettingsScreen(
    navigator: NavController,
    vm: SettingsViewModel = koinViewModel()
) {
    val state by vm.collectAsState()
    val selectedEvent by CommonApp.selectedEvent.collectAsState()

    var showLogoutWarningDialog by remember { mutableStateOf(false) }
    if (showLogoutWarningDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutWarningDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutWarningDialog = false
                    vm.logout()
                }) {
                    Text(L.settings.general.logout.action())
                }
            },
            dismissButton = {
                Button(onClick = { showLogoutWarningDialog = false }) {
                    Text(L.settings.general.keepLoggedIn())
                }
            },
            title = {
                Text(
                    text = L.settings.general.logout.title(CommonApp.settings.organisationName)
                )
            },
            text = {
                Text(
                    text = L.settings.general.logout.desc(CommonApp.settings.organisationName),
                    textAlign = TextAlign.Center
                )
            }
        )
    }

    var showThemeSelectDialog by remember { mutableStateOf(false) }
    if (showThemeSelectDialog) {
        SingleSelectDialog(
            title = L.settings.general.darkMode.title(),
            options = AppTheme.entries,
            optionId = AppTheme::ordinal,
            optionText = { it.settingsText() },
            selected = state.currentAppTheme,
            onSelect = vm::switchTheme,
            onDismissRequest = { showThemeSelectDialog = false }
        )
    }

    var showConfirmSkipMoneyBackDialog by remember { mutableStateOf(false) }
    if (showConfirmSkipMoneyBackDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSkipMoneyBackDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmSkipMoneyBackDialog = false
                    vm.toggleSkipMoneyBackDialog(value = true, confirmed = true)
                }) {
                    Text(L.settings.payment.skipMoneyBackDialog.confirmAction())
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmSkipMoneyBackDialog = false }) {
                    Text(L.dialog.cancel())
                }
            },
            title = { Text(L.settings.payment.skipMoneyBackDialog.title()) },
            text = { Text(L.settings.payment.skipMoneyBackDialog.confirmDesc()) }
        )
    }

    vm.handleSideEffects(navigator) {
        when (it) {
            SettingsEffect.ConfirmSkipMoneyBackDialog -> showConfirmSkipMoneyBackDialog = true
        }
    }

    ScaffoldView(
        title = L.settings.title(),
        topBarActions = {
            IconButton(onClick = { showLogoutWarningDialog = true }) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout")
            }
        },
        navigationIcon = {
            IconButton(onClick = { navigator.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
    ) {
        val uriHandler = LocalUriHandler.current

        LazyColumn {
            settingsSection(L.settings.general.title()) {
                settingsItem(
                    icon = { Icon(Icons.Filled.Logout, contentDescription = "Logout") },
                    title = L.settings.general.logout.action(),
                    subtitle = "\"${CommonApp.settings.organisationName}\" / \"${CommonApp.settings.waiterName}\"",
                    onClick = { showLogoutWarningDialog = true }
                )
                settingsItem(
                    icon = { Icon(Icons.Outlined.Groups, contentDescription = "Switch event") },
                    title = L.switchEvent.title(),
                    subtitle = CommonApp.settings.eventName,
                    onClick = vm::switchEvent
                )
                settingsItem(
                    icon = { Icon(Icons.Outlined.DarkMode, contentDescription = "Use dark") },
                    title = L.settings.general.darkMode.title(),
                    subtitle = state.currentAppTheme.settingsText(),
                    onClick = { showThemeSelectDialog = true }
                )
                settingsItem(
                    icon = {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Refresh data"
                        )
                    },
                    title = L.settings.general.refresh.title(),
                    subtitle = L.settings.general.refresh.desc(),
                    onClick = vm::refreshAll
                )
            }

            settingsSection(L.settings.payment.title()) {
                settingsItem(
                    icon = { Icon(Icons.Outlined.CurrencyExchange, contentDescription = null) },
                    title = L.settings.payment.skipMoneyBackDialog.title(),
                    subtitle = L.settings.payment.skipMoneyBackDialog.desc(),
                    action = {
                        Switch(
                            checked = state.skipMoneyBackDialog,
                            onCheckedChange = vm::toggleSkipMoneyBackDialog
                        )
                    },
                    onClick = vm::toggleSkipMoneyBackDialog
                )
                settingsItem(
                    icon = { Icon(Icons.Outlined.SelectAll, contentDescription = null) },
                    title = L.settings.payment.selectAllProductsByDefault.title(),
                    subtitle = L.settings.payment.selectAllProductsByDefault.desc(),
                    action = {
                        Switch(
                            checked = state.paymentSelectAllProductsByDefault,
                            onCheckedChange = vm::togglePaymentSelectAllProductsByDefault
                        )
                    },
                    onClick = vm::togglePaymentSelectAllProductsByDefault
                )
                if (selectedEvent?.stripeSettings is Event.StripeSettings.Enabled) {
                    settingsItem(
                        icon = {
                            Icon(
                                Icons.Outlined.Contactless,
                                contentDescription = L.settings.payment.cardPayment.title()
                            )
                        },
                        title = L.settings.payment.cardPayment.title(),
                        subtitle = L.settings.payment.cardPayment.desc(),
                        onClick = vm::initializeContactlessPayment
                    )
                }
            }

            settingsSection(L.settings.about.title()) {
                settingsItem(
                    icon = { Icon(Icons.Filled.PrivacyTip, contentDescription = "Privacy") },
                    title = L.settings.about.privacyPolicy(),
                    onClick = { uriHandler.openUri(CommonApp.privacyPolicyUrl) }
                )
                settingsItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = "App info") },
                    title = L.settings.about.version.title(),
                    subtitle = state.versionString
                )
            }
        }
    }
}
