package com.you.plot.feature.route.detail.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.common.utils.countryFlag
import com.you.plot.core.domain.entity.Waypoint
import com.you.plot.core.ui.dialog.PickerDialog
import com.you.plot.core.ui.general.displayLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteInfoPanel(
    dist: Double,
    elevGainM: Double,
    elevLossM: Double,
    elevationPoints: List<ElevationPoint>,
    sportType: SportType,
    isRoundTrip: Boolean,
    waypoints: List<Waypoint> = emptyList(),
    createdAt: String? = null,
    description: String = "",
    onSportTypeChange: (SportType) -> Unit = {},
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
            onConfirm = {
                onSportTypeChange(it);
                showSportDialog = false
            },
        )
    }

    Column(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
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
                dist = dist,
                elevGainM = elevGainM,
                elevLossM = elevLossM,
                elevationPoints = elevationPoints,
                sportType = sportType,
                onSportTypeChange = { showSportDialog = true },
                isRoundTrip = isRoundTrip,
                createdAt = createdAt,
                description = description,
            )

            1 -> WaypointsTab(
                waypoints = waypoints,
                totalDist = dist,
                elevationPoints = elevationPoints,
            )
        }
    }
}

@Composable
private fun WaypointsTab(
    waypoints: List<Waypoint>,
    totalDist: Double,
    elevationPoints: List<ElevationPoint>,
) {
    val ordered = waypoints.sortedBy { it.orderIndex }
    val gains = remember(ordered, elevationPoints) {
        computeSegmentGains(ordered, elevationPoints)
    }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
    ) {
        item { WaypointTableHeader() }
        items(ordered.size) { idx ->
            WaypointTableRow(
                wp = ordered[idx],
                total = totalDist,
                gainSinceLast = gains[idx],
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun WaypointTableHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderCell("Name", Modifier.weight(2f))
        HeaderCell("Dist", Modifier.weight(1f), TextAlign.End)
        HeaderCell("Elev.↑", Modifier.weight(1f), TextAlign.End)
        HeaderCell("%", Modifier.weight(0.6f), TextAlign.End)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
}

@Composable
private fun HeaderCell(
    label: String,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start
) {
    Text(
        label,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        textAlign = align,
    )
}

@Composable
private fun WaypointTableRow(wp: Waypoint, total: Double, gainSinceLast: Double) {
    val pct = if (total > 0) (wp.distFromStart / total * 100).toInt().coerceIn(0, 100) else 0
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (wp.countryCode.isNotBlank()) {
                Text(countryFlag(wp.countryCode), style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                wp.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            "%.1f km".format(wp.distFromStart),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
        )
        Text(
            if (gainSinceLast > 0) "%.0f m".format(gainSinceLast) else "—",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End,
        )
        Text(
            "$pct%",
            modifier = Modifier.weight(0.6f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * For each waypoint (in order), integrate positive elevation deltas along the
 * elevation profile from the prior waypoint up to this one. Index 0 is the
 * starting point so its gain is 0.
 */
private fun computeSegmentGains(
    waypoints: List<Waypoint>,
    profile: List<ElevationPoint>,
): List<Double> {
    if (waypoints.isEmpty() || profile.size < 2) return List(waypoints.size) { 0.0 }
    val gains = DoubleArray(waypoints.size)
    for (i in 1 until waypoints.size) {
        val from = waypoints[i - 1].distFromStart
        val to = waypoints[i].distFromStart
        var gain = 0.0
        var prevElev: Double? = null
        for (pt in profile) {
            if (pt.dist < from) continue
            if (pt.dist > to) break
            val e = pt.elevation
            if (prevElev != null && e > prevElev) gain += e - prevElev
            prevElev = e
        }
        gains[i] = gain
    }
    return gains.toList()
}
