package org.datepollsystems.waiterrobot.shared.features.table.domain

import org.datepollsystems.waiterrobot.shared.core.data.AbstractUseCase
import org.datepollsystems.waiterrobot.shared.features.table.domain.repository.TableGroupRepository

internal class HideTableGroupUseCases(
    private val tableGroupRepository: TableGroupRepository,
) : AbstractUseCase() {
    suspend fun toggle(groupId: Long) {
        tableGroupRepository.toggleGroupFilter(groupId)
    }

    suspend fun hideAll() {
        tableGroupRepository.hideAllGroups()
    }

    suspend fun showAll() {
        tableGroupRepository.showAllGroups()
    }
}
