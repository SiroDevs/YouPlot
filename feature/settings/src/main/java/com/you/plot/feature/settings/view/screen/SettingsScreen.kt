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

package com.you.plot.feature.settings.view.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.common.utils.AppConstants
import com.you.plot.core.data.repos.ThemeRepo
import com.you.plot.core.designsystem.theme.ThemeSelectorDialog
import com.you.plot.core.designsystem.theme.themeName
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.core.ui.components.action.ToggleItem
import com.you.plot.core.ui.components.dialog.PickerDialog
import com.you.plot.core.ui.components.general.InfoDivider
import com.you.plot.core.ui.components.general.InfoItem
import com.you.plot.core.ui.components.general.InfoSection
import com.you.plot.core.ui.components.general.ValueItem
import com.you.plot.feature.settings.view.components.SpeedLimitDialog
import com.you.plot.feature.settings.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    themeRepo: ThemeRepo,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val theme = themeRepo.selectedTheme
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectorDialog(
            current = theme, onDismiss = { showThemeDialog = false },
            onThemeSelected = { themeRepo.setTheme(it); showThemeDialog = false })
    }
    if (state.showDefaultSportDialog) {
        PickerDialog(
            title = "Default Sport",
            options = SportType.entries.map {
                it to it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
            },
            selected = state.defaultSport,
            onDismiss = viewModel::dismissDefaultSportDialog,
            onConfirm = viewModel::setDefaultSport,
        )
    }
    state.editingSpeedSport?.let { sport ->
        val limits = state.sportSpeedLimits[sport] ?: return@let
        SpeedLimitDialog(
            sport = sport,
            limits = limits,
            usePace = state.usePaceForRunWalk && (sport == SportType.RUNNING || sport == SportType.WALKING),
            onDismiss = viewModel::dismissSpeedEditor,
            onSave = { min, max ->
                viewModel.setSpeedLimit(
                    sport,
                    min,
                    max
                ); viewModel.dismissSpeedEditor()
            },
        )
    }

    Scaffold(
        topBar = { AppTopBar(title = "Settings", showGoBack = true, onNavIconClick = onBack) },
    ) { padding ->
        Column(Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())) {

            InfoSection("Appearance") {
                ValueItem(
                    icon = Icons.Default.Palette, title = "Theme",
                    value = themeName(state.themeMode), onClick = { showThemeDialog = true })
            }

            InfoSection("Activity") {
                ValueItem(
                    icon = Icons.Default.DirectionsRun, title = "Default Sport",
                    value = state.defaultSport.name.lowercase().replaceFirstChar { it.uppercase() },
                    onClick = viewModel::showDefaultSportDialog
                )
                InfoDivider()
                ToggleItem(
                    icon = Icons.Default.Straighten,
                    title = "Distance Unit",
                    subtitle = if (state.distanceUnitMetric) "Kilometres (km)" else "Miles (mi)",
                    checked = state.distanceUnitMetric,
                    onCheckedChange = viewModel::setDistanceUnitMetric
                )
                InfoDivider()
                ToggleItem(
                    icon = Icons.Default.Speed,
                    title = "Use Pace for Running/Walking",
                    subtitle = if (state.usePaceForRunWalk) "Displaying min/km" else "Displaying km/h",
                    checked = state.usePaceForRunWalk,
                    onCheckedChange = viewModel::setUsePaceForRunWalk
                )
            }

            InfoSection("Speed Limits") {
                Text(
                    "Set speed range shown in sliders for each sport.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                SportType.entries.forEachIndexed { index, sport ->
                    val limits = state.sportSpeedLimits[sport]
                    val usePace =
                        state.usePaceForRunWalk && (sport == SportType.RUNNING || sport == SportType.WALKING)
                    ValueItem(
                        icon = sport.settingsIcon,
                        title = sport.name.lowercase().replaceFirstChar { it.uppercase() },
                        value = if (limits != null) {
                            if (usePace)
                                "${kmhToPace(limits.minKmh)} – ${kmhToPace(limits.maxKmh)} min/km"
                            else
                                "${limits.minKmh.toInt()} – ${limits.maxKmh.toInt()} km/h"
                        } else "Default",
                        onClick = { viewModel.showSpeedEditor(sport) },
                    )
                    if (index < SportType.entries.lastIndex) InfoDivider()
                }
            }

            InfoSection("Notifications") {
                ToggleItem(
                    icon = Icons.Default.Notifications, title = "Plan Reminders",
                    subtitle = "Receive reminders for upcoming plans",
                    checked = state.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }

            InfoSection("App") {
                InfoItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    value = AppConstants.APP_VERSION
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

fun kmhToPace(kmh: Float): String {
    if (kmh <= 0f) return "–"
    val secPerKm = 3600f / kmh
    val min = (secPerKm / 60).toInt()
    val sec = (secPerKm % 60).toInt()
    return "%d:%02d".format(min, sec)
}

fun kmhToPaceStr(kmh: Float): String {
    if (kmh <= 0f) return "–"
    val secPerKm = 3600f / kmh
    val min = (secPerKm / 60).toInt()
    val sec = (secPerKm % 60).toInt()
    return "%d:%02d min/km".format(min, sec)
}

private val SportType.settingsIcon: ImageVector
    get() = when (this) {
        SportType.RUNNING -> Icons.Default.DirectionsRun
        SportType.CYCLING -> Icons.Default.DirectionsBike
        SportType.HIKING -> Icons.Default.TrendingUp
        SportType.WALKING -> Icons.Default.DirectionsWalk
    }
