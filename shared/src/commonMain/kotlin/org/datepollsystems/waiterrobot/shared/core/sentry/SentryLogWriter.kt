package org.datepollsystems.waiterrobot.shared.core.sentry

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.SentryLevel
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb

class SentryLogWriter : LogWriter() {
    override fun isLoggable(tag: String, severity: Severity): Boolean = when (severity) {
        Severity.Verbose, Severity.Debug -> false
        Severity.Info, Severity.Warn, Severity.Error, Severity.Assert -> true
    }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        when (severity) {
            Severity.Verbose, Severity.Debug -> return
            Severity.Info -> {
                Sentry.configureScope {
                    it.addBreadcrumb(
                        Breadcrumb.info(message).apply {
                            if (throwable != null) {
                                setData("exception", throwable.toString())
                            }
                        }
                    )
                }
            }

            Severity.Warn, Severity.Error, Severity.Assert -> {
                if (throwable != null) {
                    Sentry.captureException(throwable) { scope ->
                        scope.level = severity.toSentryLevel()
                        scope.addBreadcrumb(
                            Breadcrumb.error("$tag: $message")
                                .apply { level = severity.toSentryLevel() }
                        )
                        if (throwable is ExceptionWithData) {
                            scope.setContext("Exception Data", throwable.data)
                        }
                    }
                } else {
                    Sentry.captureMessage("$tag: $message") { scope ->
                        scope.level = severity.toSentryLevel()
                    }
                }
            }
        }
    }

    private fun Severity.toSentryLevel(): SentryLevel = when (this) {
        Severity.Verbose, Severity.Debug -> SentryLevel.DEBUG
        Severity.Info -> SentryLevel.INFO
        Severity.Warn -> SentryLevel.WARNING
        Severity.Error, Severity.Assert -> SentryLevel.ERROR
    }
}
