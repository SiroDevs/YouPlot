package com.you.plot.feature.route.plotter.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.AppSpecs
import com.you.plot.core.common.utils.COUNTRY_LIST
import com.you.plot.core.common.entity.WaypointSearchResult

@Composable
fun LocationSearchBar(
    query: String,
    onQryChange: () -> Unit,
    onSearch: (String) -> Unit,
    results: List<WaypointSearchResult>,
    isSearching: Boolean,
    placeholder: String,
    onResultSelected: (WaypointSearchResult) -> Unit,
    modifier: Modifier = Modifier,
    selectedCountryCode: String = "ke",
    onCountrySelected: ((String) -> Unit)? = null,
    onChooseOnMap: (() -> Unit)? = null,
    onUseMyLocation: (() -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    var showCountryMenu by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val showQuickActions = isFocused && (onChooseOnMap != null || onUseMyLocation != null)
    val hasDropdown = showQuickActions || results.isNotEmpty()
    val countryLabel =
        COUNTRY_LIST.firstOrNull { it.first == selectedCountryCode }?.first?.uppercase()
            ?.ifEmpty { "ALL" } ?: "KE"

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
            onValueChange = {},
            placeholder = { Text(placeholder) },
            leadingIcon = {
                if (isSearching) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onQryChange) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (onCountrySelected != null) {
                        Box {
                            TextButton(
                                onClick = { showCountryMenu = true },
                                modifier = Modifier.padding(end = 4.dp),
                            ) {
                                Text(
                                    countryLabel.ifEmpty { "🌍" },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select country",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            DropdownMenu(
                                expanded = showCountryMenu,
                                onDismissRequest = { showCountryMenu = false },
                            ) {
                                COUNTRY_LIST.forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    code.uppercase().ifEmpty { "🌍" },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                                Text(
                                                    name,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        },
                                        onClick = {
                                            onCountrySelected(code)
                                            showCountryMenu = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    isFocused = false

                    onSearch(query)
                }
            ),
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
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = "Choose on the Map",
                            onClick = {
                                keyboardController?.hide(); focusManager.clearFocus(); isFocused =
                                false; action()
                            },
                        )
                        HorizontalDivider(
                            Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                    onUseMyLocation?.let { action ->
                        QuickActionRow(
                            icon = {
                                Icon(
                                    Icons.Default.MyLocation,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = "Use your location",
                            onClick = {
                                keyboardController?.hide(); focusManager.clearFocus(); isFocused =
                                false; action()
                            },
                        )
                        if (results.isNotEmpty()) HorizontalDivider(
                            Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                results.forEachIndexed { index, result ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardController?.hide(); focusManager.clearFocus()
                                isFocused = false; onResultSelected(result)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            result.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (index < results.lastIndex)
                        HorizontalDivider(
                            Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                }
            }
        }
    }
}
