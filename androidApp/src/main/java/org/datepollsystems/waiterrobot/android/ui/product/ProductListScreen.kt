package org.datepollsystems.waiterrobot.android.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.android.ui.common.CenteredText
import org.datepollsystems.waiterrobot.android.ui.common.sectionHeader
import org.datepollsystems.waiterrobot.android.ui.core.ErrorBar
import org.datepollsystems.waiterrobot.android.ui.core.Preview
import org.datepollsystems.waiterrobot.android.ui.core.view.LoadingView
import org.datepollsystems.waiterrobot.android.util.desaturateOnDarkMode
import org.datepollsystems.waiterrobot.android.util.getContentColor
import org.datepollsystems.waiterrobot.android.util.toColor
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.GroupedProducts
import org.datepollsystems.waiterrobot.shared.features.product.domain.model.Product
import org.datepollsystems.waiterrobot.shared.features.product.presentation.list.ProductListState
import org.datepollsystems.waiterrobot.shared.features.product.presentation.list.ProductListViewModel
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.allGroups
import org.datepollsystems.waiterrobot.shared.generated.localization.noProductFound
import org.datepollsystems.waiterrobot.shared.generated.localization.placeholder
import org.datepollsystems.waiterrobot.shared.generated.localization.title
import org.datepollsystems.waiterrobot.shared.utils.cent
import org.datepollsystems.waiterrobot.shared.utils.euro
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun ProductListScreen(
    vm: ProductListViewModel = koinViewModel(),
    onSelect: (Product) -> Unit,
    close: () -> Unit,
) {
    val state by vm.collectAsState()

    ProductList(
        state = state,
        onSelect = onSelect,
        onFilter = vm::filterProducts,
        refresh = vm::refreshProducts,
        close = close
    )
}

@Composable
private fun ProductList(
    state: ProductListState,
    onSelect: (Product) -> Unit,
    onFilter: (String) -> Unit,
    refresh: () -> Unit,
    close: () -> Unit,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = close) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
            }

            // TODO replace with Searchbar when available (https://m3.material.io/components/search/overview)
            OutlinedTextField(
                value = state.filter,
                onValueChange = onFilter,
                label = { Text(L.productSearch.title()) },
                placeholder = { Text(L.productSearch.placeholder()) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, "Search product")
                },
                trailingIcon = {
                    IconButton(
                        enabled = state.filter.isNotEmpty(),
                        onClick = { onFilter("") }
                    ) {
                        Icon(imageVector = Icons.Outlined.Clear, "Clear search")
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                modifier = Modifier
                    .padding(start = 10.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
                    .fillMaxWidth()
            )
        }

        val productGroupsResource = state.productGroups
        if (productGroupsResource is Resource.Loading && productGroupsResource.data == null) {
            LoadingView()
        } else {
            Column {
                if (productGroupsResource is Resource.Error) {
                    ErrorBar(message = productGroupsResource.userMessage, retryAction = refresh)
                }
                val productGroups = productGroupsResource.data
                if (productGroups.isNullOrEmpty()) {
                    CenteredText(text = L.productSearch.noProductFound(), scrollAble = false)
                } else {
                    val coScope = rememberCoroutineScope()
                    val pagerState = rememberPagerState {
                        productGroups.size + 1 // One additional "all" page
                    }

                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 0.dp,
                        divider = {} // Add divider externally as otherwise it does not span the whole width
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = { coScope.launch { pagerState.scrollToPage(0) } },
                            text = { Text(L.productSearch.allGroups()) }
                        )
                        productGroups.forEachIndexed { index, productGroup ->
                            val backgroundColor =
                                productGroup.color.toColor()?.desaturateOnDarkMode()
                            val textColor = backgroundColor?.getContentColor() ?: Color.Unspecified
                            Tab(
                                modifier = Modifier.background(
                                    backgroundColor ?: Color.Unspecified
                                ),
                                selected = pagerState.currentPage == index + 1,
                                onClick = { coScope.launch { pagerState.scrollToPage(index + 1) } },
                                text = { Text(text = productGroup.name, color = textColor) }
                            )
                        }
                    }

                    HorizontalDivider()

                    HorizontalPager(pagerState) { pageIndex ->
                        if (pageIndex == 0) {
                            if (productGroups.all { it.products.isEmpty() }) {
                                CenteredText(text = "No products", scrollAble = false)
                            } else {
                                ProductLazyVerticalGrid {
                                    productGroups.forEach { productGroup ->
                                        if (productGroup.products.isNotEmpty()) {
                                            sectionHeader(
                                                key = "group-${productGroup.id}",
                                                title = productGroup.name
                                            )
                                            items(
                                                productGroup.products,
                                                key = Product::id
                                            ) { product ->
                                                Product(
                                                    product = product,
                                                    groupColor = productGroup.color.toColor(),
                                                    onSelect = { onSelect(product) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (productGroups[pageIndex - 1].products.isEmpty()) {
                                CenteredText(text = "No products", scrollAble = false)
                            } else {
                                ProductLazyVerticalGrid {
                                    val productGroup = productGroups[pageIndex - 1]
                                    items(productGroup.products, key = Product::id) { product ->
                                        Product(
                                            product = product,
                                            groupColor = productGroup.color.toColor(),
                                            onSelect = { onSelect(product) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductLazyVerticalGrid(
    content: LazyGridScope.() -> Unit
) = LazyVerticalGrid(
    modifier = Modifier.fillMaxSize(),
    columns = GridCells.Adaptive(100.dp),
    contentPadding = PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    content = content
)

@Preview
@Composable
private fun ProductListPreview() = Preview {
    ProductList(
        state = ProductListState(
            productGroups = Resource.Loading(
                data = listOf(
                    GroupedProducts(
                        id = 1,
                        name = "Group 1",
                        color = "#FF0000",
                        position = 1,
                        products = listOf(
                            Product(
                                id = 1,
                                name = "Product 1",
                                price = 1.euro,
                                soldOut = false,
                                color = "#FF0000",
                                position = 1,
                                allergens = emptyList()
                            ),
                            Product(
                                id = 2,
                                name = "Product 2",
                                price = 2.euro,
                                soldOut = false,
                                color = "#FF0000",
                                position = 2,
                                allergens = emptyList()
                            )
                        )
                    ),
                    GroupedProducts(
                        id = 2,
                        name = "Group 2",
                        color = "#00FF00",
                        position = 2,
                        products = listOf(
                            Product(
                                id = 3,
                                name = "Product 3",
                                price = 350.cent,
                                soldOut = false,
                                color = "#00FF00",
                                position = 1,
                                allergens = emptyList()
                            ),
                            Product(
                                id = 4,
                                name = "Product 4",
                                price = 4.euro,
                                soldOut = false,
                                color = "#00FF00",
                                position = 2,
                                allergens = emptyList()
                            )
                        )
                    )
                )
            ),
            filter = ""
        ),
        onSelect = {},
        onFilter = {},
        refresh = {},
        close = {}
    )
}
