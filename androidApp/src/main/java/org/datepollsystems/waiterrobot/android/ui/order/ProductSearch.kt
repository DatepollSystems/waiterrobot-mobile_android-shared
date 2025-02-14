package org.datepollsystems.waiterrobot.android.ui.order

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.datepollsystems.waiterrobot.android.ui.common.CenteredText
import org.datepollsystems.waiterrobot.android.ui.common.sectionHeader
import org.datepollsystems.waiterrobot.android.ui.core.ErrorBar
import org.datepollsystems.waiterrobot.android.ui.core.view.LoadingView
import org.datepollsystems.waiterrobot.android.util.desaturateOnDarkMode
import org.datepollsystems.waiterrobot.android.util.getContentColor
import org.datepollsystems.waiterrobot.android.util.toColor
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.Product
import org.datepollsystems.waiterrobot.shared.features.order.domain.model.ProductGroup
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.allGroups
import org.datepollsystems.waiterrobot.shared.generated.localization.noProductFound
import org.datepollsystems.waiterrobot.shared.generated.localization.placeholder
import org.datepollsystems.waiterrobot.shared.generated.localization.title

@Composable
fun ProductSearch(
    productGroupsResource: Resource<List<ProductGroup>>,
    onSelect: (Product) -> Unit,
    onFilter: (String) -> Unit,
    close: () -> Unit,
    refreshProducts: () -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = close) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
            }

            // TODO replace with Searchbar when available (https://m3.material.io/components/search/overview)
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    onFilter(it)
                },
                label = { Text(L.productSearch.title()) },
                placeholder = { Text(L.productSearch.placeholder()) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, "Search product")
                },
                trailingIcon = {
                    IconButton(
                        enabled = text.isNotEmpty(),
                        onClick = {
                            text = ""
                            onFilter("")
                        }
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

        if (productGroupsResource is Resource.Loading && productGroupsResource.data == null) {
            LoadingView()
        } else {
            if (productGroupsResource is Resource.Error) {
                ErrorBar(message = productGroupsResource.userMessage, retryAction = refreshProducts)
            }
            val productGroups = productGroupsResource.data
            if (productGroups.isNullOrEmpty()) {
                CenteredText(text = L.productSearch.noProductFound(), scrollAble = false)
            } else {
                val coScope = rememberCoroutineScope()
                val pagerState =
                    rememberPagerState { productGroups.size + 1 } // One additional "all" page

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
                            modifier = Modifier.background(backgroundColor ?: Color.Unspecified),
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
