package org.datepollsystems.waiterrobot.shared.core.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.di.getLogger
import org.datepollsystems.waiterrobot.shared.core.settings.Tokens
import org.datepollsystems.waiterrobot.shared.features.auth.repository.AuthRepository
import io.ktor.client.plugins.logging.Logger as KtorLogger

// Use a wrapper class to make it typeSafe and not require to rely on named dependencies in koin.
// This also allows the usage of the nicer koin constructor DSL
class AuthorizedClient(val delegate: HttpClient)

internal fun createAuthorizedClient(
    json: Json,
    ktorLogger: KtorLogger,
    authRepository: AuthRepository,
    enableNetworkLogs: Boolean = false,
): AuthorizedClient = AuthorizedClient(
    HttpClient {
        commonConfig(json, ktorLogger, enableNetworkLogs)

        install(Auth) {
            bearer {
                loadTokens {
                    authRepository.getTokens().toBearerTokens()
                }

                // Function to refresh a token (called when server response with 401 and a WWW-Authenticate header)
                refreshTokens {
                    @Suppress("TooGenericExceptionCaught")
                    try {
                        authRepository.refreshTokens().toBearerTokens()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // TODO improve request errors handling (-> try again, no connection info)
                        CommonApp.getLogger("AuthorizedClientTokenRefresh").e(e) {
                            "Error while refreshing token"
                        }
                        CommonApp.logout()
                        null
                    }
                }
            }
        }
    }
)

private fun Tokens?.toBearerTokens(): BearerTokens? = this?.let {
    BearerTokens(it.accessToken, it.refreshToken)
}
