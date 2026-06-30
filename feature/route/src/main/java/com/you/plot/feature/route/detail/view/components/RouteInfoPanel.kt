package com.you.plot.feature.route.detail.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.NordicWalking
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.ui.components.dialog.PickerDialog
import com.you.plot.feature.route.plotter.view.components.ElevationProfileGraph

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteInfoPanel(
    distanceKm: Double,
    elevGainM: Double,
    elevLossM: Double,
    elevationProfile: List<ElevationPoint>,
    sportType: SportType,
    isRoundTrip: Boolean,
    waypoints: List<Waypoint> = emptyList(),
    createdAt: String? = null,
    description: String = "",
    onSportTypeChange: (SportType) -> Unit = {},
    onRoundTripChange: (Boolean) -> Unit = {},
    modifier: Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSportDialog by remember { mutableStateOf(false) }
    val tabs = if (waypoints.isEmpty()) listOf("Overview") else listOf("Overview", "Waypoints")

    if (showSportDialog) {
        PickerDialog(
            title = "Sport Type",
            options = SportType.entries.map { it to it.displayLabel },
            selected = sportType,
            onDismiss = { showSportDialog = false },
            onConfirm = { onSportTypeChange(it); showSportDialog = false },
        )
    }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = { Text(title) },
                )
            }
        }

        when (selectedTab) {
            0 -> OverviewTab(
                distanceKm = distanceKm,
                elevGainM = elevGainM,
                elevLossM = elevLossM,
                elevationProfile = elevationProfile,
                sportType = sportType,
                isRoundTrip = isRoundTrip,
                createdAt = createdAt,
                description = description,
            )
            1 -> WaypointsTab(waypoints = waypoints, totalDistanceKm = distanceKm)
        }
    }
}

@Composable
private fun WaypointsTab(waypoints: List<Waypoint>, totalDistanceKm: Double) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
    ) {
        items(waypoints.sortedBy { it.orderIndex }) { wp ->
            RouteDetailWaypointRow(wp = wp, total = totalDistanceKm)
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}
