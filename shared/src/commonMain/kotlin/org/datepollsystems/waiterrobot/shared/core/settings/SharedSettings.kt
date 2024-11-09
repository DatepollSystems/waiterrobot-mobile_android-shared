package org.datepollsystems.waiterrobot.shared.core.settings

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.nullableString
import com.russhwolf.settings.string
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.datepollsystems.waiterrobot.shared.features.auth.api.models.LoginResponseDto
import org.datepollsystems.waiterrobot.shared.features.settings.models.AppTheme
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalSettingsApi::class)
class SharedSettings : KoinComponent {
    private val settings: ObservableSettings by inject()

    val eventName get() = selectedEvent?.name ?: "Unknown"
    val selectedEventId: Long get() = selectedEvent?.id ?: -1L

    var organisationName: String by settings.string(defaultValue = "Unknown")
        internal set
    var waiterName: String by settings.string(defaultValue = "Unknown")
        internal set
    var lastUpdateAvailableNote: Instant? by settings.nullableJsonSerialized()

    internal var enableContactlessPayment: Boolean by settings.boolean(defaultValue = true)
    internal var apiBase: String? by settings.nullableString()
    internal var waiterId: String? by settings.nullableString()

    // Can not use the settings "native" serialization as this currently can not be combined with settings flow
    internal var tokens: Tokens? by settings.nullableJsonSerialized()
    internal val tokenFlow: Flow<Tokens?> =
        settings.jsonSerializedOrNullFlow(SharedSettings::tokens.name)

    internal var selectedEvent: Event? by settings.nullableJsonSerialized()
    internal val selectedEventFlow: Flow<Event?> =
        settings.jsonSerializedOrNullFlow(SharedSettings::selectedEvent.name)

    internal var theme: AppTheme by settings.jsonSerialized(defaultValue = AppTheme.SYSTEM)
    internal val themeFlow: Flow<AppTheme> =
        settings.jsonSerializedFlow(SharedSettings::theme.name, AppTheme.SYSTEM)

    internal var skipMoneyBackDialog: Boolean by settings.boolean(defaultValue = false)
    internal val skipMoneyBackDialogFlow: Flow<Boolean> =
        settings.getBooleanFlow(SharedSettings::skipMoneyBackDialog.name, defaultValue = false)

    internal var paymentSelectAllProductsByDefault: Boolean by settings.boolean(defaultValue = false)
    internal val paymentSelectAllProductsByDefaultFlow: Flow<Boolean> =
        settings.getBooleanFlow(
            SharedSettings::paymentSelectAllProductsByDefault.name,
            defaultValue = false
        )
}

@Serializable
internal data class Tokens(
    val accessToken: String,
    val refreshToken: String
) {
    companion object {
        fun fromLoginResponse(response: LoginResponseDto): Tokens =
            Tokens(response.accessToken, response.refreshToken)
    }
}
