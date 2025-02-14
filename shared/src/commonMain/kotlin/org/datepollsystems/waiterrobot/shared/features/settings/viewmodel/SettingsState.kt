package org.datepollsystems.waiterrobot.shared.features.settings.viewmodel

import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.features.settings.models.AppTheme
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.desc

data class SettingsState(
    val currentAppTheme: AppTheme = CommonApp.settings.theme,
    val skipMoneyBackDialog: Boolean = CommonApp.settings.skipMoneyBackDialog,
    val paymentSelectAllProductsByDefault: Boolean = CommonApp.settings.paymentSelectAllProductsByDefault,
) : ViewModelState {
    val versionString
        get() = L.settings.about.version.desc(
            CommonApp.appInfo.appVersion,
            CommonApp.appInfo.appBuild.toString()
        )
}
