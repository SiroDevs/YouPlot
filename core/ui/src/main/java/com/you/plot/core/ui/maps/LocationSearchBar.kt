/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.you.plot.core.ui.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.AppSpecs
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.domain.entity.WaypointSearchResult

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
    selectedCtryCode: String = "ke",
    onCountrySelected: ((String) -> Unit)? = null,
    onChooseOnMap: (() -> Unit)? = null,
    onUseMyLocation: (() -> Unit)? = null,
    // Only when there is at least one saved start point does the bookmark icon
    // appear; tapping it opens the searchable start-point picker.
    savedStartPoints: List<StartPoint> = emptyList(),
    onStartPointPicked: ((StartPoint) -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var typed by remember { mutableStateOf(query) }
    LaunchedEffect(query) { if (query != typed) typed = query }
    var showStartPointPicker by remember { mutableStateOf(false) }
    val showBookmark = savedStartPoints.isNotEmpty() && onStartPointPicked != null

    if (showStartPointPicker) {
        StartPointPickerDialog(
            startPoints = savedStartPoints,
            onDismiss = { showStartPointPicker = false },
            onPicked = { sp ->
                onStartPointPicked?.invoke(sp)
                showStartPointPicker = false
            },
        )
    }

    val showQuickActions = isFocused && (onChooseOnMap != null || onUseMyLocation != null)
    val hasDropdown = showQuickActions || results.isNotEmpty()
    val showClearButton = typed.isNotEmpty()

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
            value = typed,
            onValueChange = { typed = it },
            placeholder = { Text(placeholder) },
            leadingIcon = {
                if (isSearching) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showClearButton) {
                        IconButton(onClick = {
                            typed = ""
                            onQryChange()
                        }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (showBookmark) {
                        IconButton(onClick = { showStartPointPicker = true }) {
                            Icon(
                                Icons.Outlined.Bookmark,
                                contentDescription = "Saved start points",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    if (onCountrySelected != null) {
                        CountrySelector(
                            selectedCtryCode = selectedCtryCode,
                            onCountrySelected = onCountrySelected
                        )
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

                    onSearch(typed)
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
