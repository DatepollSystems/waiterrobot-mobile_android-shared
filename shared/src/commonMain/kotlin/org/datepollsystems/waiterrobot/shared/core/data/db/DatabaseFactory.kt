package org.datepollsystems.waiterrobot.shared.core.data.db

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.AllergenEntity
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.ProductEntity
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.ProductGroupEntity
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.TableEntity
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.TableGroupEntity
import kotlin.reflect.KClass

fun createRealmDB(): Realm {
    val schema: Set<KClass<out RealmObject>> = setOf(
        TableGroupEntity::class,
        TableEntity::class,
        ProductGroupEntity::class,
        ProductEntity::class,
        AllergenEntity::class,
    )

    @Suppress("MagicNumber")
    val config = RealmConfiguration.Builder(schema)
        // Realm is only used as a persistent cache - so do not care about migrations
        .deleteRealmIfMigrationNeeded()
        // TODO increase with each version of the common code
        //  (automate - compute from version in buildScript or app version?)
        .schemaVersion(4)
        .build()

    return Realm.open(config)
}
