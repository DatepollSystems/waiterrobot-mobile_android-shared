package org.datepollsystems.waiterrobot.shared.features.order.data.local.entity

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.Instant
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.ProductGroup
import org.datepollsystems.waiterrobot.shared.utils.extensions.Now

internal class ProductGroupEntity() : RealmObject {
    @PrimaryKey
    var id: Long = -1
    var name: String = ""
    var eventId: Long = -1
    var position: Int = Int.MAX_VALUE
    var products: RealmList<ProductEntity> = realmListOf()
    var color: String? = null
    var updatedAt: Long = 0L

    val updated: Instant
        get() = Instant.fromEpochMilliseconds(updatedAt)

    constructor(
        id: Long,
        name: String,
        eventId: Long,
        position: Int,
        color: String?,
        products: List<ProductEntity>,
        updatedAt: Instant = Now()
    ) : this() {
        this.id = id
        this.name = name
        this.eventId = eventId
        this.position = position
        this.color = color
        this.products = products.toRealmList()
        this.updatedAt = updatedAt.toEpochMilliseconds()
    }

    fun toModel(productNameFilter: String?) = ProductGroup(
        id = this.id,
        name = this.name,
        position = this.position,
        color = this.color,
        products = when {
            productNameFilter.isNullOrBlank() -> this.products
            else -> this.products.filter { it.name.contains(productNameFilter, ignoreCase = true) }
        }.toModels()
    )
}

internal fun List<ProductGroupEntity>.toModels(productNameFilter: String?) =
    this.sorted().map { it.toModel(productNameFilter) }

internal fun List<ProductGroupEntity>.sorted() = this.sortedWith(
    compareBy(ProductGroupEntity::position).thenBy { it.name.lowercase() }
)
