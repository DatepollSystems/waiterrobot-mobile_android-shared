package org.datepollsystems.waiterrobot.shared.features.table.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table

internal class TableEntity() : RealmObject {
    @PrimaryKey
    var id: Long = -1
    var number: Int = -1
    var hasOrders: Boolean = false

    constructor(
        id: Long,
        number: Int,
    ) : this() {
        this.id = id
        this.number = number
    }

    fun toModel(groupName: String) = Table(
        id = this.id,
        number = this.number,
        hasOrders = this.hasOrders,
        groupName = groupName,
    )
}

internal fun List<TableEntity>.toModels(groupName: String) = this.map { it.toModel(groupName) }
internal fun List<TableEntity>.sorted() = this.sortedBy(TableEntity::number)
