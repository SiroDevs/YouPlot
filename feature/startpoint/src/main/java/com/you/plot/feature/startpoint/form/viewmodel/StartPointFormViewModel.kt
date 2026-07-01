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

package com.you.plot.feature.startpoint.form.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.data.repos.PlotterRepo
import com.you.plot.core.domain.entity.StartPoint
import com.you.plot.core.domain.entity.WaypointSearchResult
import com.you.plot.core.domain.usecase.startpoint.GetStartPointByIdUseCase
import com.you.plot.core.domain.usecase.startpoint.SaveStartPointUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

data class StartPointFormUiState(
    val id: Long = 0L,
    val query: String = "",
    val results: List<WaypointSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val isReverseGeocoding: Boolean = false,
    val countryCode: String = "ke",
    val position: LatLng? = null,
    val name: String = "",
    val isSaving: Boolean = false,
    val savedId: Long? = null,
    val needsLocationPermission: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class StartPointFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val plotterRepo: PlotterRepo,
    private val saveStartPoint: SaveStartPointUseCase,
    private val getById: GetStartPointByIdUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(StartPointFormUiState())
    val state: StateFlow<StartPointFormUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        val editingId = savedStateHandle.get<Long>("startPointId") ?: 0L
        if (editingId > 0L) viewModelScope.launch {
            val existing = getById(editingId) ?: return@launch
            _state.update {
                it.copy(
                    id = existing.id,
                    name = existing.name,
                    position = existing.position,
                    query = existing.name,
                    countryCode = existing.countryCode.ifBlank { "ke" },
                )
            }
        }
    }

    fun setCountryCode(code: String) = _state.update { it.copy(countryCode = code) }
    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun clearError() = _state.update { it.copy(error = null) }
    fun clearNeedsPermission() = _state.update { it.copy(needsLocationPermission = false) }

    fun onQryClear() {
        _state.update { it.copy(query = "", results = emptyList()) }
        searchJob?.cancel()
    }

    fun onSearch(query: String) {
        _state.update { it.copy(query = query, results = emptyList()) }
        if (query.length < 3) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _state.update { it.copy(isSearching = true) }
            val cc = _state.value.countryCode
            val results = if (cc.isNotBlank())
                plotterRepo.searchLocationsWithCountry(query, cc)
            else plotterRepo.searchLocations(query)
            _state.update { it.copy(isSearching = false, results = results) }
        }
    }

    fun onResultSelected(result: WaypointSearchResult) {
        _state.update {
            it.copy(
                position = result.latLng,
                name = result.displayName.substringBefore(",").trim(),
                query = result.displayName,
                results = emptyList(),
            )
        }
    }

    fun onMapTap(latLng: LatLng) {
        _state.update { it.copy(position = latLng, isReverseGeocoding = true) }
        viewModelScope.launch {
            val name = withContext(Dispatchers.IO) {
                plotterRepo.reverseGeocode(latLng) ?: plotterRepo.resolveAreaLabel(latLng)
            }
            _state.update { it.copy(name = name, query = name, isReverseGeocoding = false) }
        }
    }

    @SuppressLint("MissingPermission")
    fun onUseMyLocation() {
        val hasPermission =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            _state.update { it.copy(needsLocationPermission = true) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            val latLng = withContext(Dispatchers.IO) { resolveCurrentLocation() }
            if (latLng == null) {
                _state.update { it.copy(isSearching = false, error = "Couldn't get your location.") }
                return@launch
            }
            _state.update { it.copy(isReverseGeocoding = true) }
            val geocodedName = withContext(Dispatchers.IO) { plotterRepo.reverseGeocode(latLng) }
                ?: plotterRepo.resolveAreaLabel(latLng)
            _state.update {
                it.copy(
                    isSearching = false, isReverseGeocoding = false,
                    position = latLng, name = geocodedName, query = geocodedName,
                )
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(needsLocationPermission = false) }
        if (granted) onUseMyLocation()
        else _state.update { it.copy(error = "Location permission denied.") }
    }

    fun save() {
        val s = _state.value
        val pos = s.position ?: run {
            _state.update { it.copy(error = "Pick a location first") }; return
        }
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "Give this start point a name") }; return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                saveStartPoint(
                    StartPoint(
                        id = s.id,
                        name = s.name,
                        position = pos,
                        countryCode = s.countryCode,
                    )
                )
            }.onSuccess { id -> _state.update { it.copy(isSaving = false, savedId = id) } }
                .onFailure { e -> _state.update { it.copy(isSaving = false, error = e.message) } }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun resolveCurrentLocation(): LatLng? = withContext(Dispatchers.IO) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) return@withContext null
        val twoMinMs = 2 * 60 * 1000L
        providers.firstNotNullOfOrNull { p ->
            lm.getLastKnownLocation(p)?.takeIf { System.currentTimeMillis() - it.time < twoMinMs }
        }?.let { return@withContext LatLng(it.latitude, it.longitude) }

        val live = suspendCancellableCoroutine<Location?> { cont ->
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    lm.removeUpdates(this); if (cont.isActive) cont.resume(loc)
                }

                @Deprecated("Deprecated in API 29")
                override fun onStatusChanged(p: String?, s: Int, e: Bundle?) {
                }
            }
            try {
                lm.requestLocationUpdates(
                    providers.first(), 0L, 0f, listener, Looper.getMainLooper(),
                )
                cont.invokeOnCancellation { lm.removeUpdates(listener) }
                viewModelScope.launch {
                    delay(5_000)
                    lm.removeUpdates(listener)
                    if (cont.isActive) cont.resume(null)
                }
            } catch (_: SecurityException) {
                cont.resume(null)
            }
        }
        live?.let { LatLng(it.latitude, it.longitude) }
            ?: providers.firstNotNullOfOrNull { lm.getLastKnownLocation(it) }
                ?.let { LatLng(it.latitude, it.longitude) }
    }
}
