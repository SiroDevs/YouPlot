package com.you.plot.feature.settings.utils

import com.you.plot.core.common.entity.SportType
import com.you.plot.core.data.repos.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val distanceUnitMetric: Boolean = true,
    val defaultSport: SportType = SportType.RUNNING,
    val showThemeDialog: Boolean = false,
    val showDefaultSportDialog: Boolean = false,
)
