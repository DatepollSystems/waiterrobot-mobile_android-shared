package org.datepollsystems.waiterrobot.shared.features.order.data.local.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

internal class AllergenEntity() : RealmObject {
    @PrimaryKey
    var id: Long = -1
    var name: String = ""
    var shortName: String = ""

    constructor(id: Long, name: String, shortName: String) : this() {
        this.id = id
        this.name = name
        this.shortName = shortName
    }
}
