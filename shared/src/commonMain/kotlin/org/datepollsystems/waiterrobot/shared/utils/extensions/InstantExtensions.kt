package org.datepollsystems.waiterrobot.shared.utils.extensions

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal fun Instant.olderThan(age: Duration, now: Instant = Now()) = this < now.minus(age)

@Suppress("FunctionName")
internal fun Now() = Clock.System.now()
