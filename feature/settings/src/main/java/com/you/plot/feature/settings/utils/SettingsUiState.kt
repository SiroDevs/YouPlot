package com.you.plot.feature.settings.utils

import com.you.plot.core.common.entity.SportType
import com.you.plot.core.data.repos.ThemeMode

data class SportSpeedLimits(
    val minKmh: Float,
    val maxKmh: Float,
)

// Default speed ranges per sport (in km/h)
val DEFAULT_SPEED_LIMITS = mapOf(
    SportType.RUNNING to SportSpeedLimits(3f, 30f),   // 20 min/km → 2 min/km
    SportType.CYCLING to SportSpeedLimits(5f, 75f),
    SportType.HIKING  to SportSpeedLimits(1f, 12f),   // ~60 min/km → 5 min/km
    SportType.WALKING to SportSpeedLimits(1f, 12f),   // 60 min/km → 5 min/km
)

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val distanceUnitMetric: Boolean = true,
    val defaultSport: SportType = SportType.RUNNING,
    val showThemeDialog: Boolean = false,
    val showDefaultSportDialog: Boolean = false,
    val usePaceForRunWalk: Boolean = true,
    val sportSpeedLimits: Map<SportType, SportSpeedLimits> = DEFAULT_SPEED_LIMITS,
    val editingSpeedSport: SportType? = null,
)
