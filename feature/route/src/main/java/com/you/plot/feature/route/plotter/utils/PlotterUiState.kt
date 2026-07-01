package com.you.plot.feature.route.plotter.utils

import com.you.plot.core.common.entity.DestinationMode
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.common.entity.PlotterStage
import com.you.plot.core.common.entity.RouteCandidate
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.WaypointSearchResult

data class PlotterUiState(
    val stage: PlotterStage = PlotterStage.STAGE_1,
    val searchQuery: String = "",
    val searchResults: List<WaypointSearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val isReverseGeocoding: Boolean = false,
    val selectedCtryCode: String = "ke",
    val startPoint: LatLng? = null,
    val startPointName: String = "",
    val destinationMode: DestinationMode = DestinationMode.PICK_POINT,
    val endPoint: LatLng? = null,
    val endPointName: String = "",
    val targetDist: Double = 10.0,
    val targetDistQry: String = "10",
    val distSuggestions: List<WaypointSearchResult> = emptyList(),
    val manualWaypoints: List<LatLng> = emptyList(),
    val suggestedWaypoints: List<LatLng> = emptyList(),
    val useSuggestedWaypoints: Boolean = false,
    val routeCandidates: List<RouteCandidate> = emptyList(),
    val selectedCandidateId: Int? = null,
    val isRoundTrip: Boolean = false,
    val name: String = "",
    val description: String = "",
    val sportType: SportType = SportType.RUNNING,
    val isSaving: Boolean = false,
    val savedRouteId: Long? = null,
    val needsLocationPermission: Boolean = false,
    val error: String? = null,
) {
    val activeWaypoints: List<LatLng>
        get() = if (useSuggestedWaypoints) suggestedWaypoints else manualWaypoints

    val selectedCandidate: RouteCandidate?
        get() = routeCandidates.firstOrNull { it.id == selectedCandidateId }
            ?: routeCandidates.firstOrNull()
}
