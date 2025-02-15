package org.datepollsystems.waiterrobot.shared.features.order.data.local.entity

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.Allergen
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.Product
import org.datepollsystems.waiterrobot.shared.utils.Cents
import org.datepollsystems.waiterrobot.shared.utils.cent

internal class ProductEntity() : RealmObject {
    @PrimaryKey
    var id: Long = -1
    var name: String = ""
    var price: Cents = 0
    var soldOut: Boolean = false
    var color: String? = null
    var allergens: RealmList<AllergenEntity> = realmListOf()
    var position: Int = Int.MAX_VALUE

    @Suppress("LongParameterList")
    constructor(
        id: Long,
        name: String,
        price: Cents,
        soldOut: Boolean,
        color: String?,
        allergens: List<AllergenEntity>,
        position: Int,
    ) : this() {
        this.id = id
        this.name = name
        this.price = price
        this.soldOut = soldOut
        this.color = color
        this.allergens = allergens.toRealmList()
        this.position = position
    }

    fun toModel() = Product(
        id = this.id,
        name = this.name,
        price = this.price.cent,
        soldOut = this.soldOut,
        color = this.color,
        allergens = this.allergens.map { Allergen(it.id, it.name, it.shortName) },
        position = this.position
    )
}

internal fun List<ProductEntity>.toModels() = this.sorted().map(ProductEntity::toModel)
internal fun List<ProductEntity>.sorted() = this.sortedWith(
    compareBy(ProductEntity::position).thenBy { it.name.lowercase() }
)
