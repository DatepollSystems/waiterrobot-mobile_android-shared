package org.datepollsystems.waiterrobot.shared.core.sentry

import io.sentry.kotlin.multiplatform.Scope
import io.sentry.kotlin.multiplatform.SentryEvent
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import org.datepollsystems.waiterrobot.shared.core.CommonApp.settings
import org.datepollsystems.waiterrobot.shared.utils.extensions.toUrl

enum class SentryTag {
    ORGANIZATION_ID, EVENT_ID, OS, OS_NAME
}

inline fun Scope.setTag(tag: SentryTag, value: String) = setTag(tag.name, value)
inline fun Scope.removeTag(tag: SentryTag) = removeTag(tag.name)

internal fun sentryBeforeSendEvent(event: SentryEvent): SentryEvent = event.apply {
    // Environment can change at any time but can't be configured via the scope so load it when sending
    environment = settings.apiBase?.toUrl()?.host ?: "unknown"
}

internal fun sentryBeforeBreadcrumb(breadcrumb: Breadcrumb): Breadcrumb? {
    if (breadcrumb.type in excludedBreadcrumbTypes) return null
    return breadcrumb
}

private val excludedBreadcrumbTypes =
    setOf("device.event", "touch", "ui.lifecycle", "app.lifecycle", "network.event")

open class ExceptionWithData(
    override val message: String? = null,
    final override val cause: Throwable? = null,
    data: Map<String, Any?> = emptyMap()
) : Exception() {
    constructor(
        message: String? = null,
        cause: Exception? = null,
        data: Pair<String, Any?>
    ) : this(message, cause, mapOf(data))

    /** Contains extra data from this exception, and all causes */
    val data: Map<String, Any?> = data + (cause?.getAdditionalData() ?: emptyMap())

    private fun Throwable.getAdditionalData(): Map<String, Any?> = when {
        this is ExceptionWithData -> this.data
        cause != null -> cause!!.getAdditionalData()
        else -> emptyMap()
    }
}
