package org.datepollsystems.waiterrobot.shared.features.order.domain

import kotlinx.coroutines.flow.first
import org.datepollsystems.waiterrobot.shared.core.data.AbstractUseCase
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.Product
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.ProductRepository

internal class GetProductUseCase(
    private val productRepository: ProductRepository,
) : AbstractUseCase() {
    suspend operator fun invoke(id: Long): Product? = productRepository.getProductById(id).first()
}

