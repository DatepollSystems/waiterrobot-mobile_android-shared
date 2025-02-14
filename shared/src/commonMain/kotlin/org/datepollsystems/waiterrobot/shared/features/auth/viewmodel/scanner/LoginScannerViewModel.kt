package org.datepollsystems.waiterrobot.shared.features.auth.viewmodel.scanner

import kotlinx.coroutines.CancellationException
import org.datepollsystems.waiterrobot.shared.core.navigation.Screen
import org.datepollsystems.waiterrobot.shared.core.viewmodel.AbstractViewModel
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState
import org.datepollsystems.waiterrobot.shared.features.auth.repository.AuthRepository
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.desc
import org.datepollsystems.waiterrobot.shared.generated.localization.title
import org.datepollsystems.waiterrobot.shared.utils.DeepLink
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce

class LoginScannerViewModel internal constructor(
    private val authRepository: AuthRepository
) : AbstractViewModel<LoginScannerState, LoginScannerEffect>(LoginScannerState()) {

    fun onCode(code: String) = intent {
        @Suppress("TooGenericExceptionCaught")
        try {
            when (val deepLink = DeepLink.createFromUrl(code)) {
                is DeepLink.Auth.LoginLink -> {
                    reduce { state.copy(viewState = ViewState.Loading) }
                    authRepository.loginWaiter(deepLink)
                    reduce { state.copy(viewState = ViewState.Idle) }
                }

                is DeepLink.Auth.RegisterLink -> {
                    navigator.push(Screen.RegisterScreen(deepLink))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.d(e) { "Error with scanned login code: $code" }
            reduce {
                state.copy(
                    viewState = ViewState.Error(
                        L.login.invalidCode.title(),
                        L.login.invalidCode.desc(),
                        onDismiss = {
                            intent { reduce { state.copy(viewState = ViewState.Idle) } }
                        }
                    )
                )
            }
        }
    }

    fun goBack() = intent {
        navigator.pop()
    }

    override suspend fun onUnhandledException(exception: Throwable) {
        TODO("Not yet implemented")
    }
}
