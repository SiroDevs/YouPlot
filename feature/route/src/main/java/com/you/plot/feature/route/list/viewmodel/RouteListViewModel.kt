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

package com.you.plot.feature.route.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you.plot.core.domain.entity.LatLng
import com.you.plot.core.domain.entity.Route
import com.you.plot.core.domain.entity.SportType
import com.you.plot.core.domain.usecase.route.GetAllRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RouteListUiState(
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = true,
)

data class RoutePlotterUiState(
    val name: String = "",
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,
    val startPoint: LatLng? = null,
    val endPoint: LatLng? = null,
    val waypoints: List<LatLng> = emptyList(),
    val isRoundTrip: Boolean = false,
    val isSaving: Boolean = false,
    val savedRouteId: Long? = null,
    val error: String? = null,
)

@HiltViewModel
class RouteListViewModel @Inject constructor(
    getAllRoutesUseCase: GetAllRoutesUseCase,
) : ViewModel() {
    val uiState: StateFlow<RouteListUiState> = getAllRoutesUseCase()
        .map { RouteListUiState(routes = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RouteListUiState())
}