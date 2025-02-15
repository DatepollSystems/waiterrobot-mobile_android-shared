package org.datepollsystems.waiterrobot.shared.features.product.presentation.list

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.shared.core.viewmodel.AbstractViewModel
import org.datepollsystems.waiterrobot.shared.features.product.domain.GetProductGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.product.domain.RefreshProductGroupsUseCase
import org.orbitmvi.orbit.syntax.simple.blockingIntent
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.repeatOnSubscription
import org.orbitmvi.orbit.syntax.simple.subIntent

class ProductListViewModel internal constructor(
    private val getProductGroupsUseCase: GetProductGroupsUseCase,
    private val refreshProductGroupsUseCase: RefreshProductGroupsUseCase,
) : AbstractViewModel<ProductListState, ProductListEffect>(ProductListState()) {
    override suspend fun onCreate() = subIntent {
        coroutineScope {
            launch {
                repeatOnSubscription {
                    @OptIn(ExperimentalCoroutinesApi::class)
                    this@ProductListViewModel.container.stateFlow
                        .map { it.filter }
                        .distinctUntilChanged()
                        .transformLatest {
                            emitAll(getProductGroupsUseCase(it))
                        }.collect { resource ->
                            reduce { state.copy(productGroups = resource) }
                        }
                }
            }
        }
    }

    fun filterProducts(filter: String) = blockingIntent {
        reduce { state.copy(filter = filter) }
    }

    fun refreshProducts() = intent {
        reduce { state.copy(productGroups = state.productGroups.loading()) }
        refreshProductGroupsUseCase().onFailure { error ->
            reduce { state.copy(productGroups = state.productGroups.error(error)) }
        }
    }

    override suspend fun onUnhandledException(exception: Throwable) {
        TODO("Not yet implemented")
    }
}
