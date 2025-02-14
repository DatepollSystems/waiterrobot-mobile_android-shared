package org.datepollsystems.waiterrobot.shared.features.table.data.local.entity

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.Instant
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.GroupedTables
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.TableGroup
import org.datepollsystems.waiterrobot.shared.utils.extensions.Now
import org.datepollsystems.waiterrobot.shared.utils.extensions.olderThan
import kotlin.time.Duration.Companion.hours

internal class TableGroupEntity() : RealmObject {
    @PrimaryKey
    var id: Long = -1
    var name: String = ""
    var eventId: Long = -1
    var position: Int = Int.MAX_VALUE
    var color: String? = null
    var hidden: Boolean = false
    var tables: RealmList<TableEntity> = realmListOf()
    var updatedAt: Long = 0L

    val updated: Instant
        get() = Instant.fromEpochMilliseconds(updatedAt)

    constructor(
        id: Long,
        name: String,
        eventId: Long,
        position: Int,
        color: String?,
        tables: List<TableEntity>,
        updatedAt: Instant = Now()
    ) : this() {
        this.id = id
        this.name = name
        this.eventId = eventId
        this.position = position
        this.color = color
        this.tables = tables.toRealmList()
        this.updatedAt = updatedAt.toEpochMilliseconds()
    }

    fun shouldRefresh(now: Instant): Boolean = updated.olderThan(1.hours, now)

    fun toModel() = GroupedTables(
        id = this.id,
        name = this.name,
        eventId = this.eventId,
        color = this.color,
        tables = this.tables.sorted().toModels(this.name),
    )

    fun toGroup() = TableGroup(
        id = this.id,
        name = this.name,
        color = this.color,
        hidden = this.hidden,
    )
}

internal fun List<TableGroupEntity>.toModels() = this.map(TableGroupEntity::toModel)
internal fun List<TableGroupEntity>.toGroups() = this.map(TableGroupEntity::toGroup)
internal fun List<TableGroupEntity>.sorted() = this.sortedBy(TableGroupEntity::position)
