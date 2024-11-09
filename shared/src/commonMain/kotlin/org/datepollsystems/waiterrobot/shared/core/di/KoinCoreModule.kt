package org.datepollsystems.waiterrobot.shared.core.di

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import co.touchlab.kermit.platformLogWriter
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.shared.core.data.api.createAuthorizedClient
import org.datepollsystems.waiterrobot.shared.core.data.api.createBasicClient
import org.datepollsystems.waiterrobot.shared.core.data.db.createRealmDB
import org.datepollsystems.waiterrobot.shared.core.sentry.SentryLogWriter
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

private val apiClientQualifier = named("apiClient")

fun Scope.getApiClient(): HttpClient {
    return get(HttpClient::class, apiClientQualifier)
}

internal val coreModule = module {
    val baseLogger = Logger(
        // TODO different severity for debug and release build?
        StaticConfig(
            Severity.Verbose,
            logWriterList = listOf(platformLogWriter(), SentryLogWriter())
        ),
        tag = "WaiterRobot"
    )
    factory { (tag: String?) -> if (tag != null) baseLogger.withTag(tag) else baseLogger }

    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single { createJson() }
    single {
        createBasicClient(
            json = get(),
            logger = CustomKtorLogger("basic"),
            enableNetworkLogs = true
        )
    }
    single {
        createAuthorizedClient(
            json = get(),
            ktorLogger = CustomKtorLogger("authorized"),
            authRepository = get(),
            enableNetworkLogs = true,
        )
    }

    single { createRealmDB() }
}

private fun createJson() = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true // Use default value for null (when not-nullable) and unknown values
}
