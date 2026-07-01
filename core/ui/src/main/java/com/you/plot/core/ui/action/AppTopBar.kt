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

package com.you.plot.core.ui.action

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    tagline: String? = null,
    centered: Boolean = false,
    showGoBack: Boolean = false,
    showNavDrawer: Boolean = false,
    onNavIconClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    stepCurrent: Int? = null,
    stepTotal: Int? = null,
) {
    require(!(showGoBack && showNavDrawer)) {
        "showGoBack and showNavDrawer cannot both be true"
    }

    val showStepIndicator = stepCurrent != null && stepTotal != null

    val titleContent: @Composable () -> Unit = {
        Column(
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (tagline != null) {
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (showStepIndicator) {
                Spacer(Modifier.height(6.dp))
                _root_ide_package_.com.you.plot.core.ui.action.StepPills(
                    current = stepCurrent!!,
                    total = stepTotal!!,
                    showLabel = true
                )
            }
        }
    }

    val navIcon: @Composable () -> Unit = {
        when {
            showGoBack -> IconButton(onClick = { onNavIconClick?.invoke() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go Back",
                )
            }
            showNavDrawer -> IconButton(onClick = { onNavIconClick?.invoke() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                )
            }
        }
    }

    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )

    if (centered) {
        CenterAlignedTopAppBar(
            title = titleContent,
            navigationIcon = navIcon,
            actions = actions,
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = colors,
        )
    } else {
        TopAppBar(
            title = titleContent,
            navigationIcon = navIcon,
            actions = actions,
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = colors,
        )
    }
}

@Composable
fun StepPills(
    current: Int,
    total: Int,
    showLabel: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            i < current  -> MaterialTheme.colorScheme.primary
                            i == current -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            else         -> MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
            )
        }
        if (showLabel) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${current + 1}/$total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StepIndicator(
    current: Int,
    total: Int,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        _root_ide_package_.com.you.plot.core.ui.action.StepPills(current = current, total = total)

        if (showLabel) {
            Spacer(Modifier.height(2.dp))
            Text(
                "Step ${current + 1} of $total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}