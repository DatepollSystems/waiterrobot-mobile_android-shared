package org.datepollsystems.waiterrobot.shared.core

import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.pluginOrNull
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.protocol.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.shared.core.data.api.AuthorizedClient
import org.datepollsystems.waiterrobot.shared.core.di.initKoin
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.datepollsystems.waiterrobot.shared.core.navigation.Screen
import org.datepollsystems.waiterrobot.shared.core.sentry.SentryTag
import org.datepollsystems.waiterrobot.shared.core.sentry.removeTag
import org.datepollsystems.waiterrobot.shared.core.sentry.sentryBeforeBreadcrumb
import org.datepollsystems.waiterrobot.shared.core.sentry.sentryBeforeSendEvent
import org.datepollsystems.waiterrobot.shared.core.sentry.setTag
import org.datepollsystems.waiterrobot.shared.core.settings.SharedSettings
import org.datepollsystems.waiterrobot.shared.features.auth.api.AuthApi
import org.datepollsystems.waiterrobot.shared.features.billing.repository.StripeProvider
import org.datepollsystems.waiterrobot.shared.features.settings.models.AppTheme
import org.datepollsystems.waiterrobot.shared.features.switchevent.models.Event
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.KoinAppDeclaration
import kotlin.coroutines.cancellation.CancellationException

object CommonApp : KoinComponent {
    private val coroutineScope: CoroutineScope by inject()
    private val logger by injectLoggerForClass()
    val settings by lazy { SharedSettings() }

    const val privacyPolicyUrl: String = "https://my.kellner.team/info/mobile-privacypolicy"

    lateinit var appInfo: AppInfo
        private set

    internal var stripeProvider: StripeProvider? = null

    @Suppress("LongParameterList")
    fun init(
        appVersion: String,
        appBuild: Int,
        phoneModel: String,
        os: OS,
        allowedHostsCsv: String,
        stripeProvider: StripeProvider? = null,
        koinPlatformDeclaration: KoinAppDeclaration?,
    ) {
        Sentry.init { options ->
            options.dsn = "https://ae6f703d02014a2ebe206a1d43007ac0@glitchtip.kellner.team/3"
            options.environment = "unknown" // Will be set in sentryBeforeSendEvent as it can change
            options.release = "${os.name} $appVersion"
            options.beforeSend = ::sentryBeforeSendEvent
            options.beforeBreadcrumb = ::sentryBeforeBreadcrumb
        }

        initKoin(koinPlatformDeclaration) // Required for the settings to work
        logger.i("Koin initialized")

        Sentry.configureScope { scope ->
            scope.setTag(SentryTag.OS, os.toString())
            scope.setTag(SentryTag.OS_NAME, os.name)
            scope.user = settings.waiterId?.let { User(id = it) }
            settings.selectedEvent?.let { selectedEvent ->
                scope.setTag(SentryTag.ORGANIZATION_ID, selectedEvent.organisationId.toString())
                scope.setTag(SentryTag.EVENT_ID, selectedEvent.id.toString())
            }
        }

        this.appInfo = AppInfo(appVersion, appBuild, phoneModel, os, allowedHostsCsv)
        Sentry.configureScope { scope ->
            scope.setExtra("appInfo", appInfo.toString())
        }

        this.stripeProvider = stripeProvider

        logger.i("CommonApp initialization finished")
    }

    internal val isLoggedIn: StateFlow<Boolean> by lazy {
        settings.tokenFlow
            .map { it != null }
            .onEach { logger.d { "Is logged in changed: $it" } }
            .stateIn(coroutineScope, SharingStarted.Eagerly, settings.tokens != null)
    }

    val selectedEvent: StateFlow<Event?> by lazy {
        settings.selectedEventFlow
            .onEach { logger.d { "Selected event changed: $it" } }
            .stateIn(
                coroutineScope,
                started = SharingStarted.Eagerly,
                initialValue = settings.selectedEvent
            )
    }

    internal val appTheme: StateFlow<AppTheme> by lazy {
        settings.themeFlow.stateIn(
            coroutineScope,
            started = SharingStarted.Lazily,
            initialValue = settings.theme
        )
    }

    internal fun logout() {
        val tokens = settings.tokens
        if (tokens != null) {
            coroutineScope.launch {
                @Suppress("TooGenericExceptionCaught")
                try {
                    getKoin().getOrNull<AuthApi>()?.logout(tokens)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.e(e) { "Could not delete session." }
                } finally {
                    settings.apiBase = null
                }
            }
        }

        settings.tokens = null // This also triggers a change to the isLoggedInFlow
        settings.selectedEvent = null
        settings.waiterId = null
        settings.organisationName = ""
        settings.waiterName = ""
        // Reset to default, so that after next login the user will be asked again
        settings.enableContactlessPayment = true

        Sentry.configureScope { scope ->
            scope.user = null
            scope.removeTag(SentryTag.ORGANIZATION_ID)
            scope.removeTag(SentryTag.EVENT_ID)
        }

        // Clear the tokens from the client, so that they get reloaded.
        val apiClients = getKoin().getAll<AuthorizedClient>()
        apiClients.forEach {
            it.delegate.pluginOrNull(Auth)
                ?.providers
                ?.filterIsInstance<BearerAuthProvider>()
                ?.forEach(BearerAuthProvider::clearToken)
        }
    }

    fun getNextRootScreen(): Screen {
        return when {
            settings.tokens == null -> Screen.LoginScreen
            settings.selectedEvent == null -> Screen.SwitchEventScreen
            stripeProvider?.shouldInitializeTerminal() == true -> Screen.StripeInitializationScreen
            else -> Screen.TableListScreen
        }
    }

    const val MIN_UPDATE_INFO_HOURS = 24
}
