package org.datepollsystems.waiterrobot.shared.core.data.db

import co.touchlab.kermit.Logger
import io.realm.kotlin.Realm
import io.realm.kotlin.types.RealmObject
import org.datepollsystems.waiterrobot.shared.core.di.injectLoggerForClass
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal abstract class AbstractDatabase : KoinComponent {
    protected val realm: Realm by inject()
    protected val logger: Logger by injectLoggerForClass()

    protected suspend fun <T : RealmObject> Realm.updateEach(
        entries: List<T>,
        block: T.() -> Unit
    ) {
        this.write {
            entries.forEach { findLatest(it)?.block() }
        }
    }
}
