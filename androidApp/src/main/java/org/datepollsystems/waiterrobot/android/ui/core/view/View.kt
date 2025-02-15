package org.datepollsystems.waiterrobot.android.ui.core.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.datepollsystems.waiterrobot.android.ui.core.AlertDialogFromState
import org.datepollsystems.waiterrobot.android.ui.core.LocalSnackbarHostState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.StateWithViewState
import org.datepollsystems.waiterrobot.shared.core.viewmodel.ViewState

/**
 * Handles displaying errors and loading state.
 * If [onRefresh] is provided a [RefreshableView] is used and [content] therefore
 * must be scrollable. Otherwise a [LoadableView] is used.
 * @see AlertDialogFromState
 * @see RefreshableView
 * @see LoadableView
 */
@Composable
fun View(
    state: StateWithViewState,
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val viewState = state.viewState
    if (onRefresh != null) {
        RefreshableView(
            modifier = modifier,
            loading = viewState == ViewState.Loading,
            onRefresh = onRefresh,
            content = content
        )
    } else {
        LoadableView(
            modifier = modifier,
            loading = viewState == ViewState.Loading,
            content = content
        )
    }

    if (viewState is ViewState.Error) {
        AlertDialogFromState(viewState.dialog)
    }
}

/**
 * see [View]
 */
@Composable
fun View(
    state: StateWithViewState,
    paddingValues: PaddingValues,
    onRefresh: (() -> Unit)? = null,
    content: @Composable () -> Unit
) = View(state, Modifier.padding(paddingValues), onRefresh, content)

@Composable
fun ScaffoldView(
    state: StateWithViewState,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    title: String,
    topBarActions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    onRefresh: (() -> Unit)? = null,
    bottomSheet: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) = Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
        TopAppBar(
            title = { Text(title) },
            actions = topBarActions,
            navigationIcon = navigationIcon
        )
    },
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
) {
    View(state = state, paddingValues = it, onRefresh = onRefresh, content = content)
    bottomSheet?.invoke()
}

@Composable
fun ScaffoldView(
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    title: String,
    topBarActions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    bottomSheet: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) = Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
        TopAppBar(
            title = { Text(title) },
            actions = topBarActions,
            navigationIcon = navigationIcon
        )
    },
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
) {
    content(it)
    bottomSheet?.invoke()
}
