package com.you.plot.feature.settings.utils

import com.you.plot.core.common.entity.SportType
import com.you.plot.core.data.repos.ThemeMode

data class SportSpeedLimits(
    val minKmh: Float,
    val maxKmh: Float,
)

val DEFAULT_SPEED_LIMITS = mapOf(
    SportType.RUNNING to SportSpeedLimits(3f, 30f),
    SportType.CYCLING to SportSpeedLimits(5f, 75f),
    SportType.HIKING to SportSpeedLimits(2f, 12f),
    SportType.WALKING to SportSpeedLimits(1f, 12f),
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
