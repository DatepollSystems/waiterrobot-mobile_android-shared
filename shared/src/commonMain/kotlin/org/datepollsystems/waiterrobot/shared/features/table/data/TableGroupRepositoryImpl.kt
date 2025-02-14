package org.datepollsystems.waiterrobot.shared.features.table.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.repository.AbstractRepository
import org.datepollsystems.waiterrobot.shared.features.table.data.local.TableDatabase
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.TableGroupEntity
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.sorted
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.toGroups
import org.datepollsystems.waiterrobot.shared.features.table.data.local.entity.toModels
import org.datepollsystems.waiterrobot.shared.features.table.data.remote.TableApi
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.GroupedTables
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.TableGroup
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableGroupRepository
import org.datepollsystems.waiterrobot.shared.utils.extensions.Now

internal class TableGroupRepositoryImpl(
    private val tableApi: TableApi,
    private val tableDatabase: TableDatabase
) : TableGroupRepository, AbstractRepository() {
    override fun getGroupedTables(eventId: Long): Flow<Resource<List<GroupedTables>>> = cached(
        query = { tableDatabase.getForEventFlow(eventId, includeHidden = false) },
        shouldRefresh = TableGroupEntity::shouldRefresh,
        refresh = { refreshTableGroups(eventId) },
        transform = { it.sorted().toModels() }
    )

    override fun getTableGroups(eventId: Long): Flow<Resource<List<TableGroup>>> = cached(
        query = { tableDatabase.getForEventFlow(eventId, includeHidden = true) },
        shouldRefresh = TableGroupEntity::shouldRefresh,
        refresh = { refreshTableGroups(eventId) },
        transform = { it.sorted().toGroups() }
    )

    override fun hasHiddenGroups(eventId: Long): Flow<Boolean> =
        tableDatabase.hasHiddenTables(eventId)

    override suspend fun refreshTableGroups(eventId: Long) = coroutineScope {
        val timestamp = Now()
        val apiTableGroups = async { tableApi.getTableGroups(eventId) }
        val tableIdsWithOrders = async { tableApi.getUnpaidTableIds() }
        val entities = apiTableGroups.await().map { it.toEntry(timestamp) }
        tableDatabase.replace(entities, tableIdsWithOrders.await())
    }

    override suspend fun toggleGroupFilter(groupId: Long) {
        tableDatabase.toggleHidden(groupId)
    }

    override suspend fun hideAllGroups() {
        tableDatabase.hideAll()
    }

    override suspend fun showAllGroups() {
        tableDatabase.showAll()
    }
}
