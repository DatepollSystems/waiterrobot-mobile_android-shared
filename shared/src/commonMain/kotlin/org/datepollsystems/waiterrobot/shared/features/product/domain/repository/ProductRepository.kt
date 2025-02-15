package org.datepollsystems.waiterrobot.shared.features.product.domain.repository

import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.Product

interface ProductRepository {
    fun getProductById(id: Long): Flow<Product?>
}
