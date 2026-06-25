package com.you.plot.feature.settings

import androidx.lifecycle.ViewModel
import com.you.plot.core.data.repos.PrefsRepo
import com.you.plot.core.data.repos.ThemeMode
import com.you.plot.core.data.repos.ThemeRepo
import com.you.plot.core.domain.entity.SportType
import com.you.plot.feature.settings.utils.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: PrefsRepo,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            themeMode = prefsRepo.appThemeMode,
            notificationsEnabled = prefsRepo.notificationsEnabled,
            distanceUnitMetric = prefsRepo.distanceUnitMetric,
            defaultSport = runCatching {
                SportType.valueOf(prefsRepo.defaultSport)
            }.getOrDefault(SportType.RUNNING),
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

    fun showDefaultSportDialog() = _state.update { it.copy(showDefaultSportDialog = true) }
    fun dismissDefaultSportDialog() = _state.update { it.copy(showDefaultSportDialog = false) }
}
