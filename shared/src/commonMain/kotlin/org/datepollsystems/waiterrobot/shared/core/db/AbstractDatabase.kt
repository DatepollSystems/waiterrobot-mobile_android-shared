package org.datepollsystems.waiterrobot.shared.core.db

import co.touchlab.kermit.Logger
import io.realm.kotlin.Realm
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal abstract class AbstractDatabase : KoinComponent {
    protected val realm: Realm by inject()
    protected val logger: Logger by inject()
}
