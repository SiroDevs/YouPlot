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

package com.you.plot.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.data.repos.PreferencesRepo
import com.you.plot.feature.settings.utils.DEFAULT_SPEED_LIMITS
import com.you.plot.feature.settings.utils.SettingsUiState
import com.you.plot.feature.settings.utils.SportSpeedLimits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: PreferencesRepo,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            themeMode = prefsRepo.appThemeMode,
            notificationsEnabled = prefsRepo.notificationsEnabled,
            distanceUnitMetric = prefsRepo.distanceUnitMetric,
            defaultSport = runCatching { SportType.valueOf(prefsRepo.defaultSport) }.getOrDefault(
                SportType.RUNNING
            ),
            usePaceForRunWalk = prefsRepo.usePaceForRunWalk,
            sportSpeedLimits = DEFAULT_SPEED_LIMITS.mapValues { (sport, defaults) ->
                SportSpeedLimits(
                    minSpeed = prefsRepo.getSpeedMin(sport).takeIf { it > 0f } ?: defaults.minSpeed,
                    maxSpeed = prefsRepo.getSpeedMax(sport).takeIf { it > 0f } ?: defaults.maxSpeed,
                )
            },
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        prefsRepo.notificationsEnabled = enabled
        _state.update { it.copy(notificationsEnabled = enabled) }
    }

    fun setDistanceUnitMetric(metric: Boolean) {
        prefsRepo.distanceUnitMetric = metric
        _state.update { it.copy(distanceUnitMetric = metric) }
    }

    fun setDefaultSport(sport: SportType) {
        prefsRepo.defaultSport = sport.name
        _state.update { it.copy(defaultSport = sport, showDefaultSportDialog = false) }
    }

    fun setUsePaceForRunWalk(pace: Boolean) {
        prefsRepo.usePaceForRunWalk = pace
        _state.update { it.copy(usePaceForRunWalk = pace) }
    }

    fun setSpeedLimit(sport: SportType, minSpeed: Float, maxSpeed: Float) {
        prefsRepo.setSpeedMin(sport, minSpeed)
        prefsRepo.setSpeedMax(sport, maxSpeed)
        _state.update {
            it.copy(
                sportSpeedLimits = it.sportSpeedLimits + (sport to SportSpeedLimits(
                    minSpeed,
                    maxSpeed
                ))
            )
        }
    }

    fun showDefaultSportDialog() = _state.update { it.copy(showDefaultSportDialog = true) }
    fun dismissDefaultSportDialog() = _state.update { it.copy(showDefaultSportDialog = false) }
    fun showSpeedEditor(sport: SportType) = _state.update { it.copy(editingSpeedSport = sport) }
    fun dismissSpeedEditor() = _state.update { it.copy(editingSpeedSport = null) }
}