package org.datepollsystems.waiterrobot.android.ui.tablelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.datepollsystems.waiterrobot.android.ui.common.CenteredText
import org.datepollsystems.waiterrobot.android.ui.core.Preview
import org.datepollsystems.waiterrobot.android.ui.core.view.LoadingView
import org.datepollsystems.waiterrobot.android.util.desaturateOnDarkMode
import org.datepollsystems.waiterrobot.android.util.toColor
import org.datepollsystems.waiterrobot.shared.core.data.Resource
import org.datepollsystems.waiterrobot.shared.features.table.domain.model.TableGroup
import org.datepollsystems.waiterrobot.shared.features.table.presentation.filter.TableGroupFilterViewModel
import org.datepollsystems.waiterrobot.shared.generated.localization.L
import org.datepollsystems.waiterrobot.shared.generated.localization.groupFilter
import org.datepollsystems.waiterrobot.shared.generated.localization.noTableFound
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun TableGroupFilterSheet(
    vm: TableGroupFilterViewModel = koinViewModel()
) {
    val state by vm.collectAsState()

    when (val groups = state.groups) {
        is Resource.Error -> CenteredText(text = groups.userMessage, scrollAble = false)
        is Resource.Loading -> LoadingView()
        is Resource.Success -> {
            TableGroupFilter(
                groups = groups.data,
                showAll = vm::showAll,
                hideAll = vm::hideAll,
                onToggle = vm::toggleFilter
            )
        }
    }
}

@Composable
private fun TableGroupFilter(
    groups: List<TableGroup>,
    showAll: () -> Unit,
    hideAll: () -> Unit,
    onToggle: (TableGroup) -> Unit
) {
    if (groups.isEmpty()) {
        // Should not happen as open filter is only shown when there are groups
        CenteredText(text = L.tableList.noTableFound(), scrollAble = false)
    } else {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = L.tableList.groupFilter(),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.weight(1f))

            // TODO replace with SegmentedButton when available for compose
            //  (https://m3.material.io/components/segmented-buttons/overview)
            val allGroupsShown = groups.none(TableGroup::hidden)
            IconToggleButton(
                checked = allGroupsShown,
                enabled = !allGroupsShown,
                onCheckedChange = { showAll() }
            ) {
                Icon(
                    Icons.Filled.DoneAll,
                    contentDescription = "Select all groups"
                )
            }
            val allGroupsHidden = groups.all(TableGroup::hidden)
            IconToggleButton(
                checked = allGroupsHidden,
                enabled = !allGroupsHidden,
                onCheckedChange = { hideAll() }
            ) {
                Icon(
                    Icons.Filled.RemoveDone,
                    contentDescription = "Unselect all groups"
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(items = groups, key = TableGroup::id) { group ->
                TableGroupFilterRow(group, onToggle = { onToggle(group) })
            }
        }
    }
}

@Composable
private fun TableGroupFilterRow(tableGroup: TableGroup, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = tableGroup.color
                        .toColor()
                        ?.desaturateOnDarkMode() ?: Color.Transparent,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = tableGroup.name)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = !tableGroup.hidden,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
@PreviewLightDark
private fun TableGroupFilterPreview() = Preview {
    Column {
        TableGroupFilterRow(
            tableGroup = TableGroup(1, "Group 1", "#ff00ff", false),
            onToggle = {},
        )
    }
}

@Composable
@Preview
private fun TableGroupFiltersPreview() = Preview {
    Column {
        TableGroupFilter(
            groups = listOf(
                TableGroup(1, "Group 1", "#ff00ff", false),
                TableGroup(3, "Group 2", "#00ffff", false),
                TableGroup(2, "Group 3", null, false),
                TableGroup(4, "Group 4", null, true),
                TableGroup(5, "Group 5", "#ffff00", false),
                TableGroup(6, "Group 6", null, false)
            ),
            onToggle = {},
            showAll = {},
            hideAll = {}
        )
    }
}
