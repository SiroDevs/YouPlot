package com.you.plot.feature.tracker.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.ActivitySession
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.SessionStatus
import com.you.plot.core.domain.entity.WaypointProgress
import com.you.plot.core.domain.repos.LocationRepo
import com.you.plot.core.domain.usecase.tracker.CompleteSessionUseCase
import com.you.plot.core.domain.usecase.tracker.GetActiveSessionUseCase
import com.you.plot.core.domain.usecase.tracker.PauseSessionUseCase
import com.you.plot.core.domain.usecase.tracker.ResumeSessionUseCase
import com.you.plot.core.domain.usecase.tracker.StartSessionUseCase
import com.you.plot.core.domain.usecase.tracker.UpdateSessionLocationUseCase
import com.you.plot.core.domain.usecase.tracker.distanceTo
import com.you.plot.feature.tracker.utils.TrackerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_VERIFY_KM = 1.0
private const val STOP_SKIPPED_KM  = 5.0
private const val PAUSE_SPEED_THRESHOLD_KMH = 1.0
private const val PAUSE_DWELL_TICKS = 3

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
    private var elapsedSeconds = 0L
    private var sessionId = 0L
    private var stopVerifyWaypoint: WaypointProgress? = null
    private var stopVerifyStartLocation: LatLng? = null
    private var distanceSinceStop = 0.0
    private var stationaryTicks = 0

    init {
        loadOrCreateSession()
    }

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

    fun startSession() {
        val s = _state.value
        if (!s.allPermissionsGranted) {
            _state.update { it.copy(showPermissionRationale = true) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { startSessionUseCase(planId) }
                .onSuccess { id ->
                    sessionId = id
                    val session = getActiveSessionUseCase()
                    _state.update { it.copy(session = session, isLoading = false) }
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
            locationRepository.getLocationUpdates(intervalMs = 5_000L).collect { location ->
                elapsedSeconds += 5

                // Derive speed from distance delta / tick interval
                val prev = _state.value.session?.currentLocation
                val deltaDist = prev?.distanceTo(location) ?: 0.0
                val speedKmh = (deltaDist / (5.0 / 3600.0)).coerceAtMost(200.0) // cap at 200 km/h

                updateLocationUseCase(
                    sessionId = sessionId,
                    newLocation = location,
                    speedKmh = speedKmh,
                    elapsedSeconds = elapsedSeconds,
                )

                val updated = getActiveSessionUseCase()
                _state.update { it.copy(session = updated) }

                checkWaypointStopReminder(updated)
                handleStopVerification(location, speedKmh)
                handleAutoPause(speedKmh)
            }
        }
    }

    private fun checkWaypointStopReminder(session: ActivitySession?) {
        if (_state.value.showFullScreenStopReminder) return  // already showing
        val pending = _state.value.pendingStopWaypoint
        val arrived = session?.waypointProgress?.firstOrNull { wp ->
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
        stopVerifyWaypoint = wp
        stopVerifyStartLocation = _state.value.session?.currentLocation
        distanceSinceStop = 0.0
        _state.update { it.copy(showFullScreenStopReminder = false) }
    }

    fun onStopIgnored() {
        _state.update {
            it.copy(
                pendingStopWaypoint = null,
                showFullScreenStopReminder = false,
            )
        }
        stopVerifyWaypoint = null
        stopVerifyStartLocation = null
        distanceSinceStop = 0.0
    }

    private fun handleStopVerification(location: LatLng, speedKmh: Double) {
        val verifyWp = stopVerifyWaypoint ?: return
        val startLoc = stopVerifyStartLocation ?: return

        distanceSinceStop += startLoc.distanceTo(location)
        stopVerifyStartLocation = location

        when {
            distanceSinceStop >= STOP_SKIPPED_KM -> {
                // User never actually stopped → mark as skipped
                val updated = _state.value.session?.copy(
                    waypointProgress = _state.value.session!!.waypointProgress.map { wp ->
                        if (wp.waypoint.id == verifyWp.waypoint.id) wp.copy(wasSkipped = true) else wp
                    }
                )
                _state.update { it.copy(session = updated, pendingStopWaypoint = null) }
                stopVerifyWaypoint = null
            }

            distanceSinceStop >= STOP_VERIFY_KM -> {
                _state.update { it.copy(pendingStopWaypoint = null) }
                stopVerifyWaypoint = null
            }
        }
    }

    private fun handleAutoPause(speedKmh: Double) {
        val session = _state.value.session ?: return
        if (session.status != SessionStatus.IN_PROGRESS) return

        if (speedKmh < PAUSE_SPEED_THRESHOLD_KMH) {
            stationaryTicks++
            if (stationaryTicks >= PAUSE_DWELL_TICKS) {
                stationaryTicks = 0
                pauseSession()
            }
        } else {
            stationaryTicks = 0
        }
    }

    fun pauseSession() {
        locationJob?.cancel()
        stationaryTicks = 0
        viewModelScope.launch {
            pauseSessionUseCase(sessionId)
            _state.update { it.copy(session = getActiveSessionUseCase()) }
        }
    }

    fun resumeSession() {
        viewModelScope.launch {
            resumeSessionUseCase(sessionId)
            _state.update { it.copy(session = getActiveSessionUseCase()) }
            startLocationTracking()
        }
    }

    fun completeSession() {
        locationJob?.cancel()
        viewModelScope.launch {
            completeSessionUseCase(sessionId)
            _state.update { it.copy(session = getActiveSessionUseCase()) }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
    }
}
