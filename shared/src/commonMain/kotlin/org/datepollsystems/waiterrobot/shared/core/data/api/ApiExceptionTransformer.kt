package org.datepollsystems.waiterrobot.shared.core.data.api

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.di.getLogger
import org.datepollsystems.waiterrobot.shared.core.sentry.ExceptionWithData

internal fun HttpClientConfig<*>.installApiClientExceptionTransformer(json: Json) {
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, request ->
            // TODO maybe also map ServerExceptions (and also other HttpExceptions) to ApiException
            val clientException = exception as? ClientRequestException
                ?: return@handleResponseExceptionWithRequest

            val logger = CommonApp.getLogger("ApiClientExceptionTransformer")
            // Get as string and do custom serialization here, so we can fallback to a generic error
            // with the basic error information if the client does not know the codeName.
            val jsonString = clientException.response.bodyAsText()
            val apiException = try {
                json.decodeFromString<ApiException>(jsonString)
            } catch (e: SerializationException) {
                logger.w(
                    ExceptionWithData(
                        message = "Could not deserialize ApiException",
                        cause = e,
                        data = "body" to jsonString
                    )
                ) {
                    "Could not serialize ApiException. Falling back to generic ApiException. " +
                        "(${request.method} ${request.url})"
                }
                json.decodeFromString<ApiException.Generic>(jsonString)
            }

            logger.i(apiException) { "Request ClientError: ${apiException.codeName}" }

            throw apiException
        }
    }
}
