package org.datepollsystems.waiterrobot.shared.core.data

import co.touchlab.kermit.Logger
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.koin.core.component.KoinComponent

internal abstract class AbstractUseCase : KoinComponent {
    protected val logger: Logger by injectLoggerForClass()
}
