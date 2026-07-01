package com.you.plot.feature.tracker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.ActivityStatus
import com.you.plot.core.domain.entity.ActivityActivity
import com.you.plot.core.domain.repos.LocationRepo
import com.you.plot.core.domain.usecase.tracker.CompleteActivityUseCase
import com.you.plot.core.domain.usecase.tracker.GetActiveActivityUseCase
import com.you.plot.core.domain.usecase.tracker.PauseActivityUseCase
import com.you.plot.core.domain.usecase.tracker.ResumeActivityUseCase
import com.you.plot.core.domain.usecase.tracker.StartActivityUseCase
import com.you.plot.core.domain.usecase.tracker.UpdateActivityLocationUseCase
import com.you.plot.core.domain.usecase.tracker.distanceTo
import com.you.plot.feature.tracker.service.AutoPauseDetector
import com.you.plot.feature.tracker.service.StopVerifier
import com.you.plot.feature.tracker.utils.TrackerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val startActivityUseCase: StartActivityUseCase,
    private val updateLocationUseCase: UpdateActivityLocationUseCase,
    private val pauseActivityUseCase: PauseActivityUseCase,
    private val resumeActivityUseCase: ResumeActivityUseCase,
    private val completeActivityUseCase: CompleteActivityUseCase,
    private val getActiveActivityUseCase: GetActiveActivityUseCase,
    private val locationRepository: LocationRepo,
) : ViewModel() {
    private val planId: Long = checkNotNull(savedStateHandle["planId"])

    private val _state = MutableStateFlow(TrackerUiState())
    val state: StateFlow<TrackerUiState> = _state.asStateFlow()

    private var locationJob: Job? = null
    private var elapsedSeconds = 0L
    private var activityId = 0L
    private val autoPauseDetector = AutoPauseDetector()
    private val stopVerifier = StopVerifier()

    init {
        loadOrCreateActivity()
    }

    private fun loadOrCreateActivity() {
        viewModelScope.launch {
            val existing = getActiveActivityUseCase()
            if (existing != null && existing.planId == planId) {
                activityId = existing.id
                _state.update { it.copy(activity = existing, isLoading = false) }
                if (existing.status == ActivityStatus.IN_PROGRESS) startLocationTracking()
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onLocationPermissionResult(granted: Boolean) =
        _state.update { it.copy(locationPermissionGranted = granted) }

    fun onBackgroundLocationResult(granted: Boolean) =
        _state.update { it.copy(backgroundLocationGranted = granted) }

    fun onActivityRecognitionResult(granted: Boolean) =
        _state.update { it.copy(activityRecognitionGranted = granted) }

    fun onLocationServicesResult(enabled: Boolean) =
        _state.update { it.copy(locationServicesEnabled = enabled) }

    fun showPermissionRationale() =
        _state.update { it.copy(showPermissionRationale = true) }

    fun dismissPermissionRationale() =
        _state.update { it.copy(showPermissionRationale = false) }

    fun startActivity() {
        val s = _state.value
        if (!s.allPermissionsGranted) {
            _state.update { it.copy(showPermissionRationale = true) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { startActivityUseCase(planId) }
                .onSuccess { id ->
                    activityId = id
                    val activity = getActiveActivityUseCase()
                    _state.update { it.copy(activity = activity, isLoading = false) }
                    startLocationTracking()
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    private fun startLocationTracking() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationRepository.getLocationUpdates(intervalMs = LOCATION_TICK_MS).collect { location ->
                elapsedSeconds += LOCATION_TICK_SECONDS

                val prev = _state.value.activity?.currentLocation
                val deltaDist = prev?.distanceTo(location) ?: 0.0
                val speedKmh = (deltaDist / (LOCATION_TICK_SECONDS / 3600.0)).coerceAtMost(SPEED_CAP_KMH)

                updateLocationUseCase(
                    activityId = activityId,
                    newLocation = location,
                    speedKmh = speedKmh,
                    elapsedSeconds = elapsedSeconds,
                )

                val updated = getActiveActivityUseCase()
                _state.update { it.copy(activity = updated) }

                checkWaypointStopReminder(updated)
                val advance = stopVerifier.advance(location)
                when (advance.outcome) {
                    StopVerifier.Outcome.SKIPPED -> markWaypointSkipped(advance.waypoint)
                    StopVerifier.Outcome.CONFIRMED -> _state.update { it.copy(pendingStopWaypoint = null) }
                    StopVerifier.Outcome.ONGOING -> Unit
                }
                if (autoPauseDetector.shouldPause(speedKmh)) pauseActivity()
            }
        }
    }

    private fun markWaypointSkipped(waypoint: com.you.plot.core.domain.entity.WaypointProgress?) {
        val target = waypoint ?: return
        val current = _state.value.activity ?: return
        val updated = current.copy(
            waypointProgress = current.waypointProgress.map { wp ->
                if (wp.waypoint.id == target.waypoint.id) wp.copy(wasSkipped = true) else wp
            },
        )
        _state.update { it.copy(activity = updated, pendingStopWaypoint = null) }
    }

    private fun checkWaypointStopReminder(activity: ActivityActivity?) {
        if (_state.value.showFullScreenStopReminder) return  // already showing
        val pending = _state.value.pendingStopWaypoint
        val arrived = activity?.waypointProgress?.firstOrNull { wp ->
            wp.isReached &&
                !wp.wasSkipped &&
                wp.waypoint.isStopPlanned &&
                wp != pending
        }
        if (arrived != null) {
            _state.update {
                it.copy(
                    pendingStopWaypoint = arrived,
                    showFullScreenStopReminder = true,
                )
            }
        }
    }

    fun onStopAcknowledged() {
        val wp = _state.value.pendingStopWaypoint ?: return
        stopVerifier.begin(wp, _state.value.activity?.currentLocation)
        _state.update { it.copy(showFullScreenStopReminder = false) }
    }

    fun onStopIgnored() {
        _state.update {
            it.copy(
                pendingStopWaypoint = null,
                showFullScreenStopReminder = false,
            )
        }
        stopVerifier.cancel()
    }

    fun pauseActivity() {
        locationJob?.cancel()
        autoPauseDetector.reset()
        viewModelScope.launch {
            pauseActivityUseCase(activityId)
            _state.update { it.copy(activity = getActiveActivityUseCase()) }
        }
    }

    fun resumeActivity() {
        viewModelScope.launch {
            resumeActivityUseCase(activityId)
            _state.update { it.copy(activity = getActiveActivityUseCase()) }
            startLocationTracking()
        }
    }

    fun completeActivity() {
        locationJob?.cancel()
        viewModelScope.launch {
            completeActivityUseCase(activityId)
            _state.update { it.copy(activity = getActiveActivityUseCase()) }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
    }

    companion object {
        private const val LOCATION_TICK_MS = 5_000L
        private const val LOCATION_TICK_SECONDS = 5L
        private const val SPEED_CAP_KMH = 200.0
    }
}
