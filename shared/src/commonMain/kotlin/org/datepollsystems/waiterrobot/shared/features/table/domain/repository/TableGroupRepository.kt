package org.datepollsystems.waiterrobot.shared.features.table.domain.repository

import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.GroupedTables
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.TableGroup

internal interface TableGroupRepository {
    fun getGroupedTables(eventId: Long): Flow<Resource<List<GroupedTables>>>
    fun getTableGroups(eventId: Long): Flow<Resource<List<TableGroup>>>
    fun hasHiddenGroups(eventId: Long): Flow<Boolean>
    suspend fun refreshTableGroups(eventId: Long)
    suspend fun toggleGroupFilter(groupId: Long)
    suspend fun hideAllGroups()
    suspend fun showAllGroups()
}
