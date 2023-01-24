package org.datepollsystems.waiterrobot.shared.utils

/** Base on
 * - https://johnoreilly.dev/posts/kotlinmultiplatform-swift-combine_publisher-flow/
 * - https://proandroiddev.com/kotlin-multiplatform-mobile-sharing-the-ui-state-management-a67bd9a49882
 * - https://github.com/orbit-mvi/orbit-swift-gradle-plugin/blob/main/src/main/resources/Publisher.swift.mustache
 */

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

/**
 * Collects the flow and calls the [onEach] function for each received item.
 * KotlinFlow -> Callback base API
 *
 * This is needed as from Swift no coroutines can be launched and to collect a flow a coroutine (or suspendable function) is needed.
 */
@Suppress("unused") // Used by iOS
fun <T : Any> Flow<T>.subscribe(
    onEach: (item: T) -> Unit,
    onComplete: () -> Unit,
    onThrow: (error: Throwable) -> Unit
): Job = this
    .onEach { onEach(it) }
    .catch {
        Logger.withTag("FlowSubscription").i("Flow catch: ${it.message} ($it)")
        onThrow(it)
    }
    .onCompletion { onComplete() }
    .launchIn(CoroutineScope(Job() + Dispatchers.Main))
