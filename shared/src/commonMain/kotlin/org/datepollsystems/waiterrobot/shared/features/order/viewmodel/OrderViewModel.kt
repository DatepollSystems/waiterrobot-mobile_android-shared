package org.datepollsystems.waiterrobot.shared.features.order.viewmodel

import kotlinx.coroutines.coroutineScope
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.core.data.api.ApiException
import org.datepollsystems.waiterrobot.shared.core.navigation.NavOrViewModelEffect
import org.datepollsystems.waiterrobot.shared.core.navigation.Screen
import org.datepollsystems.waiterrobot.shared.core.viewmodel.AbstractViewModel
import org.datepollsystems.waiterrobot.shared.core.viewmodel.DialogState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState
import org.datepollsystems.waiterrobot.shared.features.order.data.OrderRepositoryImpl
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.OrderItem
import org.datepollsystems.waiterrobot.shared.features.product.domain.GetProductUseCase
import org.datepollsystems.waiterrobot.shared.features.product.domain.RefreshProductGroupsUseCase
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.Product
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.Table
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.desc
import org.datepollsystems.waiterrobot.shared.generated.localization.descOrderSent
import org.datepollsystems.waiterrobot.shared.generated.localization.generic
import org.datepollsystems.waiterrobot.shared.generated.localization.ok
import org.datepollsystems.waiterrobot.shared.generated.localization.title
import org.datepollsystems.waiterrobot.shared.utils.extensions.emptyToNull
import org.datepollsystems.waiterrobot.shared.utils.randomUUID
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.syntax.simple.subIntent

@Suppress("TooManyFunctions")
class OrderViewModel internal constructor(
    private val getProductUseCase: GetProductUseCase,
    private val refreshProductGroupsUseCase: RefreshProductGroupsUseCase,
    private val orderRepository: OrderRepositoryImpl,
    private val table: Table,
    private val initialItemId: Long?,
) : AbstractViewModel<OrderState, OrderEffect>(OrderState()) {

    private var currentOrderId = randomUUID()

    override suspend fun onCreate() = subIntent {
        coroutineScope {
            if (initialItemId != null) {
                addItem(initialItemId, 1)
            }
        }
    }

    fun addItem(product: Product, amount: Int) = addItem(product.id, amount)

    fun addItemNote(item: OrderItem, note: String?) = intent {
        val newItem = item.copy(note = note?.trim().emptyToNull())

        reduce {
            state.copy(
                _currentOrder = state._currentOrder.plus(newItem.product.id to newItem)
            )
        }
    }

    fun sendOrder() = intent {
        reduce { state.copy(orderingState = ViewState.Loading) }

        val order = state.currentOrder
        orderRepository.sendOrder(table, order, currentOrderId)
            .onSuccess {
                currentOrderId = randomUUID()
                navigator.popUpTo(Screen.TableDetailScreen(table), inclusive = false)
            }
            .onFailure { e ->
                when (e) {
                    is ApiException.ProductSoldOut -> {
                        val soldOutProduct = order.first { it.product.id == e.productId }.product
                        productSoldOut(soldOutProduct)
                    }

                    is ApiException.ProductStockToLow -> {
                        val stockToLowProduct = order.first { it.product.id == e.productId }.product
                        if (e.remaining <= 0) {
                            productSoldOut(stockToLowProduct)
                        } else {
                            stockToLow(stockToLowProduct, e.remaining)
                        }
                    }

                    is ApiException.OrderAlreadySubmitted -> {
                        logger.w("Order was already submitted")
                        navigator.popUpTo(Screen.TableDetailScreen(table), inclusive = false)
                    }

                    else -> {
                        logger.e(e) { "Failed to send order" }
                        reduce {
                            val dismiss: () -> Unit = {
                                intent { reduce { state.copy(orderingState = ViewState.Idle) } }
                            }
                            state.copy(
                                orderingState = ViewState.ErrorDialog(
                                    DialogState(
                                        L.exceptions.title(),
                                        L.exceptions.generic(),
                                        onDismiss = dismiss,
                                        primaryButton = DialogState.Button(L.dialog.ok(), dismiss)
                                    )
                                )
                            )
                        }
                    }
                }
            }
    }

    @Suppress("unused") // used on iOS
    fun removeAllOfProduct(productId: Long) = intent {
        reduce {
            state.copy(_currentOrder = state._currentOrder.minus(productId))
        }
    }

    fun abortOrder() = intent {
        navigator.pop()
    }

    fun addItem(id: Long, amount: Int) = intent {
        val product = getProductUseCase(id)

        if (product == null) {
            logger.w("Tried to add product with id '$id' but could not find the product.")
            reduce {
                state.copy(
                    orderingState = ViewState.ErrorDialog(
                        dialog = DialogState(
                            title = L.order.couldNotFindProduct.title(),
                            text = L.order.couldNotFindProduct.desc(),
                            onDismiss = { removeItem(id) },
                            primaryButton = DialogState.Button(
                                text = L.dialog.ok(),
                                action = { removeItem(id) }
                            )
                        )
                    )
                )
            }
            return@intent
        }

        if (product.soldOut) {
            logger.w("Tried to add product (id: $id) which is already sold out.")
            productSoldOut(product)
            return@intent
        }

        reduce {
            val item = state._currentOrder[id] ?: product.toNewOrderItem()
            val newAmount = item.amount + amount

            val newOrder = if (newAmount <= 0) {
                state._currentOrder.minus(product.id)
            } else {
                val newItem = item.copy(amount = newAmount)
                state._currentOrder.plus(newItem.product.id to newItem)
            }
            state.copy(_currentOrder = newOrder)
        }
    }

    fun removeItem(id: Long) = intent {
        reduce {
            state.copy(
                _currentOrder = state._currentOrder.minus(id)
            )
        }
    }

    private suspend fun SimpleSyntax<OrderState, NavOrViewModelEffect<OrderEffect>>.productSoldOut(
        product: Product
    ) {
        refreshProducts()
        reduce {
            state.copy(
                orderingState = ViewState.ErrorDialog(
                    dialog = DialogState(
                        title = L.order.productSoldOut.title(),
                        text = L.order.productSoldOut.descOrderSent(product.name),
                        onDismiss = { removeItem(product.id) },
                        primaryButton = DialogState.Button(
                            text = L.dialog.ok(),
                            action = { removeItem(product.id) }
                        )
                    )
                )
            )
        }
    }

    private suspend fun SimpleSyntax<OrderState, NavOrViewModelEffect<OrderEffect>>.stockToLow(
        product: Product,
        remaining: Int
    ) {
        refreshProducts()
        reduce {
            state.copy(
                orderingState = ViewState.ErrorDialog(
                    dialog = DialogState(
                        title = L.order.stockToLow.title(),
                        text = L.order.stockToLow.desc(remaining.toString(), product.name),
                        onDismiss = {},
                        primaryButton = DialogState.Button(
                            text = L.dialog.ok(),
                            action = { }
                        )
                    )
                )
            )
        }
    }

    fun refreshProducts() = intent {
        refreshProductGroupsUseCase()
    }

    private fun Product.toNewOrderItem(): OrderItem {
        require(!soldOut) { "Product is sold out, not allowed to add to an Order" }
        return OrderItem(product = this, amount = 0, note = null)
    }

    private val Resource<Map<Long, OrderItem>>.dataOrEmpty get() = this.data ?: emptyMap()

    override suspend fun onUnhandledException(exception: Throwable) {
        TODO("Not yet implemented")
    }
}
