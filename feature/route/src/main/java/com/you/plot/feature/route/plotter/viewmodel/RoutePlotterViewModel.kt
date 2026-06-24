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

package com.you.plot.feature.route.plotter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.SportType
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.domain.usecase.route.DeleteRouteUseCase
import com.you.plot.core.domain.usecase.route.SaveRouteUseCase
import com.you.plot.feature.route.list.viewmodel.RoutePlotterUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutePlotterViewModel @Inject constructor(
    private val saveRouteUseCase: SaveRouteUseCase,
    private val deleteRouteUseCase: DeleteRouteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RoutePlotterUiState())
    val state: StateFlow<RoutePlotterUiState> = _state.asStateFlow()

    fun setName(name: String) = _state.update { it.copy(name = name) }
    fun setDescription(desc: String) = _state.update { it.copy(description = desc) }
    fun setSportType(type: SportType) = _state.update { it.copy(sportType = type) }
    fun setRoundTrip(isRound: Boolean) = _state.update { it.copy(isRoundTrip = isRound) }
    fun setStartPoint(latLng: LatLng) = _state.update { it.copy(startPoint = latLng) }
    fun setEndPoint(latLng: LatLng) = _state.update { it.copy(endPoint = latLng) }
    fun addWaypoint(latLng: LatLng) = _state.update { it.copy(waypoints = it.waypoints + latLng) }
    fun removeWaypoint(index: Int) = _state.update {
        it.copy(waypoints = it.waypoints.toMutableList().also { l -> l.removeAt(index) })
    }
    fun clearError() = _state.update { it.copy(error = null) }

    fun saveRoute() {
        val s = _state.value
        val start = s.startPoint ?: run {
            _state.update { it.copy(error = "Set a start point on the map") }; return
        }
        val end = s.endPoint ?: run {
            _state.update { it.copy(error = "Set a destination on the map") }; return
        }
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching {
                val waypointList = buildWaypoints(start, s.waypoints, end)
                val route = Route(
                    name = s.name.ifBlank { "New Route" },
                    description = s.description,
                    sportType = s.sportType,
                    startPoint = start,
                    endPoint = end,
                    waypoints = waypointList,
                    totalDistanceKm = estimateDistance(start, s.waypoints, end),
                    isRoundTrip = s.isRoundTrip,
                )
                saveRouteUseCase(route)
            }.onSuccess { id ->
                _state.update { it.copy(isSaving = false, savedRouteId = id) }
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deleteRoute(id: Long) = viewModelScope.launch { deleteRouteUseCase(id) }

    private fun buildWaypoints(start: LatLng, middle: List<LatLng>, end: LatLng): List<Waypoint> {
        val all = listOf(start) + middle + listOf(end)
        return all.mapIndexed { index, latLng ->
            Waypoint(
                routeId = 0L,
                name = when (index) {
                    0 -> "Start"; all.lastIndex -> "Finish"; else -> "Waypoint $index"
                },
                position = latLng,
                orderIndex = index,
                isStopPlanned = index != 0 && index != all.lastIndex,
            )
        }
    }

    private fun estimateDistance(start: LatLng, middle: List<LatLng>, end: LatLng): Double {
        val all = listOf(start) + middle + listOf(end)
        return all.zipWithNext().sumOf { (a, b) -> haversineKm(a, b) }
    }

    private fun haversineKm(a: LatLng, b: LatLng): Double {
        val r = 6371.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val h = Math.sin(dLat / 2).let { it * it } +
            Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude)) *
            Math.sin(dLng / 2).let { it * it }
        return r * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h))
    }
}