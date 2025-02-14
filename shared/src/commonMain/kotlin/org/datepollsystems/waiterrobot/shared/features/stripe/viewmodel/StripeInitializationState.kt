package org.datepollsystems.waiterrobot.shared.features.stripe.viewmodel

import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewModelState
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.disabledForEvent
import org.datepollsystems.waiterrobot.shared.generated.localization.locationPermissionDenied
import org.datepollsystems.waiterrobot.shared.generated.localization.readerConnectionFailed
import org.datepollsystems.waiterrobot.shared.generated.localization.terminalInitiationFailed

data class StripeInitializationState(
    val step: Step = Step.Start,
    val stepIndex: Int = 1,
    val isLoading: Boolean = false,
) : ViewModelState {

    sealed class Step {
        data object Start : Step()
        data object GrantLocationPermission : Step()
        data object EnableGeoLocation : Step()
        data object EnableNfc : Step()
        data object Finished : Step()
        sealed class Error(val description: String, val retryAble: Boolean = true) : Step() {
            data object StripeDisabledForEvent :
                Error(L.stripeInit.error.disabledForEvent(), retryAble = false)

            data object GeolocationPermissionDenied :
                Error(L.stripeInit.error.locationPermissionDenied())

            data object TerminalInitializationFailed :
                Error(L.stripeInit.error.terminalInitiationFailed())

            data object ReaderConnectionFailed : Error(L.stripeInit.error.readerConnectionFailed())
        }

        companion object {
            const val COUNT: Float = 6f
        }
    }
}
