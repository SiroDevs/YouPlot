package com.you.plot.feature.route.plotter.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.AppSpecs
import com.you.plot.core.domain.entity.SearchResult

@Composable
fun LocationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<SearchResult>,
    isSearching: Boolean,
    placeholder: String,
    onResultSelected: (SearchResult) -> Unit,
    modifier: Modifier = Modifier,
    onChooseOnMap: (() -> Unit)? = null,
    onUseMyLocation: (() -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }

    val showQuickActions = isFocused && (onChooseOnMap != null || onUseMyLocation != null)
    val hasDropdown = showQuickActions || results.isNotEmpty()

    Column(
        modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (hasDropdown) 6.dp else 3.dp,
                shape = if (hasDropdown) AppSpecs.TOP_SHAPE else AppSpecs.FULL_SHAPE,
                clip = false,
            )
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder) },
            leadingIcon = {
                if (isSearching)
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else
                    Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = if (hasDropdown) AppSpecs.TOP_SHAPE else AppSpecs.FULL_SHAPE,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
        )

        if (hasDropdown) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(AppSpecs.BOTTOM_SHAPE)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (showQuickActions) {
                    onChooseOnMap?.let { action ->
                        QuickActionRow(
                            icon = {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            label = "Choose on the Map",
                            onClick = action,
                        )
                        if (onUseMyLocation != null || results.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }

                    onUseMyLocation?.let { action ->
                        QuickActionRow(
                            icon = {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            label = "Use your location",
                            onClick = action,
                        )
                        if (results.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }

                results.forEachIndexed { index, result ->
                    if (index == 0 && showQuickActions) {
                        // Separator already added above
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onResultSelected(result) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            result.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (index < results.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        icon()
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
