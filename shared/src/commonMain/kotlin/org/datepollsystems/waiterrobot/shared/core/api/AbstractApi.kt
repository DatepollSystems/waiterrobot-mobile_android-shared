package org.datepollsystems.waiterrobot.shared.core.api

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.koin.core.component.KoinComponent

internal abstract class AbstractApi(basePath: String, private val client: HttpClient) :
    KoinComponent {
    protected val logger: Logger by injectLoggerForClass()

    // Make sure that the baseUrl ends with a "/"
    private val baseUrl =
        "${CommonApp.appInfo.apiBaseUrl}v1/${basePath.removePrefix("/").removeSuffix("/")}/"

    /**
     * Prepend string (endpoint) with base and make sure that endpoint does not start with "/".
     * Also remove the trailing "/" because ".../endpoint" and ".../endpoint/" may be treated as different routes.
     */
    private fun String.toFullUrl() = (baseUrl + this.removePrefix("/")).dropLastWhile { it == '/' }

    protected suspend fun get(
        endpoint: String = "",
        vararg query: Pair<String, Any>,
        block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpResponse = client.get(endpoint.toFullUrl()) {
        query.forEach {
            url.parameters.append(it.first, it.second.toString())
        }

        block?.invoke(this)
    }

    protected suspend fun post(
        endpoint: String = "",
        body: RequestBodyDto? = null,
        block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpResponse = client.post(endpoint.toFullUrl()) {
        if (body != null) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        block?.invoke(this)
    }
}

internal abstract class AuthorizedApi(basePath: String, client: AuthorizedClient) :
    AbstractApi(basePath, client.delegate)

// Marker interface
internal interface RequestBodyDto
