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

package com.you.plot.feature.tracker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.ActivitySession
import com.you.plot.core.domain.entity.SessionStatus
import com.you.plot.core.domain.entity.WaypointProgress
import com.you.plot.core.domain.repos.LocationRepo
import com.you.plot.core.domain.usecase.tracker.CompleteSessionUseCase
import com.you.plot.core.domain.usecase.tracker.GetActiveSessionUseCase
import com.you.plot.core.domain.usecase.tracker.PauseSessionUseCase
import com.you.plot.core.domain.usecase.tracker.ResumeSessionUseCase
import com.you.plot.core.domain.usecase.tracker.StartSessionUseCase
import com.you.plot.core.domain.usecase.tracker.UpdateSessionLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackerUiState(
    val session: ActivitySession? = null,
    val isLoading: Boolean = true,
    val isLocationPermissionGranted: Boolean = false,
    val error: String? = null,
    val pendingStopWaypoint: WaypointProgress? = null,
)

@HiltViewModel
class TrackerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startSessionUseCase: StartSessionUseCase,
    private val updateLocationUseCase: UpdateSessionLocationUseCase,
    private val pauseSessionUseCase: PauseSessionUseCase,
    private val resumeSessionUseCase: ResumeSessionUseCase,
    private val completeSessionUseCase: CompleteSessionUseCase,
    private val getActiveSessionUseCase: GetActiveSessionUseCase,
    private val locationRepository: LocationRepo,
) : ViewModel() {

    private val planId: Long = checkNotNull(savedStateHandle["planId"])

    private val _state = MutableStateFlow(TrackerUiState())
    val state: StateFlow<TrackerUiState> = _state.asStateFlow()

    private var locationJob: Job? = null
    private var sessionId: Long = 0L
    private var elapsedSeconds = 0L

    init { loadOrCreateSession() }

    private fun loadOrCreateSession() {
        viewModelScope.launch {
            val existing = getActiveSessionUseCase()
            if (existing != null && existing.planId == planId) {
                sessionId = existing.id
                _state.update { it.copy(session = existing, isLoading = false) }
                if (existing.status == SessionStatus.IN_PROGRESS) startLocationTracking()
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPermissionResult(granted: Boolean) = _state.update { it.copy(isLocationPermissionGranted = granted) }

    fun startSession() {
        viewModelScope.launch {
            runCatching { startSessionUseCase(planId) }
                .onSuccess { id -> sessionId = id; _state.update { it.copy(isLoading = false) }; startLocationTracking() }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    private fun startLocationTracking() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationRepository.getLocationUpdates(intervalMs = 5_000L).collect { location ->
                elapsedSeconds += 5
                updateLocationUseCase(sessionId = sessionId, newLocation = location, speedKmh = 0.0, elapsedSeconds = elapsedSeconds)
                val updated = getActiveSessionUseCase()
                _state.update { it.copy(session = updated) }
                checkWaypointArrival(updated)
            }
        }
    }

    private fun checkWaypointArrival(session: ActivitySession?) {
        val arrived = session?.waypointProgress?.firstOrNull {
            it.isReached && !it.wasSkipped && it.waypoint.isStopPlanned && it != _state.value.pendingStopWaypoint
        }
        if (arrived != null) _state.update { it.copy(pendingStopWaypoint = arrived) }
    }

    fun onStopAcknowledged() = _state.update { it.copy(pendingStopWaypoint = null) }
    fun onStopIgnored() = _state.update { it.copy(pendingStopWaypoint = null) }

    fun pauseSession() {
        locationJob?.cancel()
        viewModelScope.launch { pauseSessionUseCase(sessionId); _state.update { it.copy(session = getActiveSessionUseCase()) } }
    }

    fun resumeSession() {
        viewModelScope.launch { resumeSessionUseCase(sessionId); startLocationTracking(); _state.update { it.copy(session = getActiveSessionUseCase()) } }
    }

    fun completeSession() {
        locationJob?.cancel()
        viewModelScope.launch { completeSessionUseCase(sessionId); _state.update { it.copy(session = getActiveSessionUseCase()) } }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    override fun onCleared() { super.onCleared(); locationJob?.cancel() }
}
