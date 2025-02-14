package org.datepollsystems.waiterrobot.shared.features.table.data.local

import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.delete
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.find
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.datepollsystems.waiterrobot.shared.core.data.db.AbstractDatabase
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.TableEntity
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.TableGroupEntity
import org.datepollsystems.waiterrobot.shared.utils.extensions.Now
import kotlin.time.Duration

internal class TableDatabase : AbstractDatabase() {

    fun getForEventFlow(eventId: Long, includeHidden: Boolean): Flow<List<TableGroupEntity>> {
        return when {
            includeHidden -> realm.query<TableGroupEntity>("eventId == $0", eventId)
            else -> realm.query<TableGroupEntity>("eventId == $0 AND hidden == $1", eventId, false)
        }.asFlow().map { it.list }
    }

    fun hasHiddenTables(eventId: Long): Flow<Boolean> {
        return realm.query<TableGroupEntity>("eventId == $0 AND hidden == $1", eventId, true)
            .count()
            .asFlow()
            .map { it > 0 }
    }

    suspend fun replace(tableGroups: List<TableGroupEntity>, tableIdsWithOrders: Set<Long>) {
        // Realm does currently not have the concept of partial update.
        // Always the whole object is overridden.
        val hiddenGroupIds = realm.query<TableGroupEntity>("hidden == $0", true)
            .find { it.mapTo(mutableSetOf(), TableGroupEntity::id) }
        tableGroups.forEach {
            it.hidden = it.id in hiddenGroupIds
            it.tables.forEach { table ->
                table.hasOrders = table.id in tableIdsWithOrders
            }
        }

        realm.write {
            delete<TableEntity>()
            delete<TableGroupEntity>()
            tableGroups.forEach { copyToRealm(it, UpdatePolicy.ALL) }
        }
    }

    suspend fun updateTablesWithOrder(tableIdsWithOrders: Set<Long>) {
        val tables = realm.query<TableEntity>().find()
        realm.updateEach(tables) {
            hasOrders = id in tableIdsWithOrders
        }
    }

    suspend fun toggleHidden(id: Long) {
        realm.write {
            val group = query<TableGroupEntity>("id == $0", id).first().find() ?: return@write
            group.hidden = !group.hidden
        }
    }

    suspend fun deleteOlderThan(maxAge: Duration) {
        val timestamp = Now().minus(maxAge).toEpochMilliseconds()
        realm.write {
            delete(query<TableGroupEntity>("updatedAt <= $0", timestamp).find())
        }
    }

    suspend fun showAll() {
        val groups = realm.query<TableGroupEntity>().find()
        realm.updateEach(groups) {
            hidden = false
        }
    }

    suspend fun hideAll() {
        val groups = realm.query<TableGroupEntity>().find()
        realm.updateEach(groups) {
            hidden = true
        }
    }
}
