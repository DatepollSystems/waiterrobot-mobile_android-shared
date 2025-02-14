package org.datepollsystems.waiterrobot.shared.features.order.data

import kotlinx.coroutines.flow.Flow
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.repository.AbstractRepository
import org.datepollsystems.waiterrobot.shared.features.order.data.local.ProductDatabase
import org.datepollsystems.waiterrobot.shared.features.order.data.local.entity.toModels
import org.datepollsystems.waiterrobot.shared.features.order.data.remote.ProductApi
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.ProductGroup
import org.datepollsystems.waiterrobot.shared.features.order.domain.repository.ProductGroupRepository
import org.datepollsystems.waiterrobot.shared.utils.extensions.Now
import org.datepollsystems.waiterrobot.shared.utils.extensions.olderThan
import org.datepollsystems.waiterrobot.shared.utils.extensions.runCatchingCancelable
import kotlin.time.Duration.Companion.hours

internal class ProductGroupRepositoryImpl(
    private val productApi: ProductApi,
    private val productDb: ProductDatabase,
) : ProductGroupRepository, AbstractRepository() {

    override fun getProductGroups(
        eventId: Long,
        filter: String?
    ): Flow<Resource<List<ProductGroup>>> = cached(
        query = { productDb.getForEventFlow(eventId) },
        shouldRefresh = { now -> updated.olderThan(maxAge, now) },
        refresh = { refreshProductGroups(eventId) },
        transform = { it.toModels(filter) }
    )

    override suspend fun refreshProductGroups(eventId: Long) = runCatchingCancelable {
        val timestamp = Now()
        val apiTableGroups = productApi.getProducts(eventId).filter { it.products.isNotEmpty() }
        val entities = apiTableGroups.map { it.toEntry(eventId, timestamp) }
        productDb.replace(entities)
    }

    companion object {
        private val maxAge = 24.hours
    }
}
