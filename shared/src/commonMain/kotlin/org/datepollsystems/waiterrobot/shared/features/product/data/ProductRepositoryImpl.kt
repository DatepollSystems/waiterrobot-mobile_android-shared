package org.datepollsystems.waiterrobot.shared.features.product.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.datepollsystems.waiterrobot.shared.core.repository.AbstractRepository
import org.datepollsystems.waiterrobot.shared.features.product.data.local.ProductDatabase
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.Product
import org.datepollsystems.waiterrobot.shared.features.product.domain.repository.ProductRepository

internal class ProductRepositoryImpl(
    private val productDatabase: ProductDatabase
) : ProductRepository, AbstractRepository() {
    override fun getProductById(id: Long): Flow<Product?> {
        return productDatabase.getProductById(id).map { it?.toModel() }
    }
}
