package org.datepollsystems.waiterrobot.shared.features.order.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.datepollsystems.waiterrobot.shared.core.repository.AbstractRepository
import org.datepollsystems.waiterrobot.shared.features.order.data.local.ProductDatabase
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.Product
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.ProductRepository

internal class ProductRepositoryImpl(
    private val productDatabase: ProductDatabase
) : ProductRepository, AbstractRepository() {
    override fun getProductById(id: Long): Flow<Product?> {
        return productDatabase.getProductById(id).map { it?.toModel() }
    }
}
