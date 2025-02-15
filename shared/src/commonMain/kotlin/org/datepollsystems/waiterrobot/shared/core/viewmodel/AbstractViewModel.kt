package org.datepollsystems.waiterrobot.shared.core.viewmodel

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import org.datepollsystems.waiterrobot.shared.core.data.api.ApiException
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.datepollsystems.waiterrobot.shared.core.navigation.NavAction
import org.datepollsystems.waiterrobot.shared.core.navigation.NavOrViewModelEffect
import org.datepollsystems.waiterrobot.shared.core.navigation.Screen
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable
import org.koin.core.component.KoinComponent
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect

abstract class AbstractViewModel<S : ViewModelState, E : ViewModelEffect>(
    initialState: S
) : ViewModel(), ContainerHost<S, NavOrViewModelEffect<E>>, KoinComponent {

    protected val logger by injectLoggerForClass()

    final override val container: Container<S, NavOrViewModelEffect<E>> = viewModelScope.container(
        initialState = initialState,
        onCreate = {
            logger.d { "Creating Orbit container" }
            onCreate()
        },
        buildSettings = {
            exceptionHandler = CoroutineExceptionHandler { _, exception ->
                when (exception) {
                    is CancellationException -> Unit // Expected, nothing to do here

                    is ApiException.AppVersionTooOld -> intent {
                        navigator.replaceRoot(Screen.UpdateApp)
                    }

                    else -> {
                        logger.w(exception) {
                            "Unhandled exception in intent. " +
                                "Exceptions should be handled directly in the intent!"
                        }
                        intent {
                            runCatchingCancelable {
                                onUnhandledException(exception)
                            }.onFailure { e ->
                                logger.e(e) { "Exception in onUnhandledException" }
                            }
                        }
                    }
                }
            }
        }
    )

    protected open suspend fun onCreate(): Unit = Unit
    protected abstract suspend fun onUnhandledException(exception: Throwable)

    @OrbitDsl
    protected val SimpleSyntax<S, NavOrViewModelEffect<E>>.navigator get() = Navigator(simpleSyntax = this)

    @OrbitDsl
    protected suspend fun SimpleSyntax<S, NavOrViewModelEffect<E>>.postSideEffect(effect: E) {
        postSideEffect(NavOrViewModelEffect.VMEffect(effect))
    }

    inner class Navigator(private val simpleSyntax: SimpleSyntax<S, NavOrViewModelEffect<E>>) {
        @OrbitDsl
        suspend fun pop() = navigate(NavAction.Pop)

        @OrbitDsl
        suspend fun push(screen: Screen) = navigate(NavAction.Push(screen))

        @OrbitDsl
        suspend fun popUpTo(screen: Screen, inclusive: Boolean) =
            navigate(NavAction.PopUpTo(screen, inclusive))

        @OrbitDsl
        suspend fun popUpAndPush(screen: Screen, popUpTo: Screen, inclusive: Boolean) =
            navigate(NavAction.PopUpAndPush(screen, popUpTo, inclusive))

        @OrbitDsl
        suspend fun replaceRoot(screen: Screen) = navigate(NavAction.ReplaceRoot(screen))

        private suspend inline fun navigate(action: NavAction) =
            simpleSyntax.postSideEffect(NavOrViewModelEffect.NavEffect(action))
    }
}
