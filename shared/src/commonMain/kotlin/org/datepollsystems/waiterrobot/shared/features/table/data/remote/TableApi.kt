package org.datepollsystems.waiterrobot.shared.features.table.data.remote

import io.ktor.client.call.body
import org.datepollsystems.waiterrobot.shared.core.CommonApp
import org.datepollsystems.waiterrobot.shared.core.data.api.AuthorizedApi
import org.datepollsystems.waiterrobot.shared.core.data.api.AuthorizedClient
import org.datepollsystems.waiterrobot.shared.features.table.data.remote.dto.TableGroupDto
import org.datepollsystems.waiterrobot.shared.features.table.data.remote.dto.UnpaidTableIdsDto

internal class TableApi(client: AuthorizedClient) : AuthorizedApi("v1/waiter/table", client) {

    suspend fun getTableGroups(eventId: Long = CommonApp.settings.selectedEventId) =
        get("group", "eventId" to eventId.toString())
            .body<List<TableGroupDto>>()

    suspend fun getUnpaidTableIds(eventId: Long = CommonApp.settings.selectedEventId) =
        get("activeOrders", "eventId" to eventId.toString())
            .body<UnpaidTableIdsDto>().tableIds
}
