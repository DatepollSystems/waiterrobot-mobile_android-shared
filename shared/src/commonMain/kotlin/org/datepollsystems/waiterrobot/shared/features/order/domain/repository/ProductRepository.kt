package org.datepollsystems.waiterrobot.shared.features.order.domain.repository

import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.Product

interface ProductRepository {
    fun getProductById(id: Long): Flow<Product?>
}
