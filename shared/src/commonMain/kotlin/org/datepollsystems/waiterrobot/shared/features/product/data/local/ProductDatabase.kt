package org.datepollsystems.waiterrobot.shared.features.product.data.local

import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.delete
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.datepollsystems.waiterrobot.shared.core.data.db.AbstractDatabase
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.AllergenEntity
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.ProductEntity
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.ProductGroupEntity
import org.datepollsystems.waiterrobot.shared.utils.extensions.Now
import kotlin.time.Duration

internal class ProductDatabase : AbstractDatabase() {

    fun getForEventFlow(eventId: Long): Flow<List<ProductGroupEntity>> =
        realm.query<ProductGroupEntity>("eventId == $0", eventId).asFlow().map { it.list }

    fun getProductById(id: Long): Flow<ProductEntity?> =
        realm.query<ProductEntity>("id == $0", id).first().asFlow().map { it.obj }

    suspend fun replace(productGroups: List<ProductGroupEntity>) {
        realm.write {
            delete<AllergenEntity>()
            delete<ProductEntity>()
            delete<ProductGroupEntity>()
            productGroups.forEach { copyToRealm(it, UpdatePolicy.ALL) }
        }
    }

    suspend fun deleteForEvent(eventId: Long) {
        realm.query<ProductGroupEntity>("eventId == $0", eventId).delete()
    }

    suspend fun deleteOlderThan(maxAge: Duration) {
        val timestamp = Now().minus(maxAge).toEpochMilliseconds()
        realm.query<ProductGroupEntity>("updatedAt <= $0", timestamp).delete()
    }

    private suspend fun RealmQuery<ProductGroupEntity>.delete() {
        val oldGroups = find()
        realm.write {
            oldGroups.forEach { group ->
                group.products.forEach { product ->
                    findLatest(product)?.also { delete(it) }
                }
                findLatest(group)?.also {
                    delete(it)
                }
            }
        }
    }
}
