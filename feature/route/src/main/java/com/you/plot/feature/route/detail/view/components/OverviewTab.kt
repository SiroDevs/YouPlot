package com.you.plot.feature.route.detail.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.ElevationPoint
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.ui.maps.ElevationProfile

@Composable
fun OverviewTab(
    distanceKm: Double,
    elevGainM: Double,
    elevLossM: Double,
    elevationPoints: List<ElevationPoint>,
    sportType: SportType,
    onSportTypeChange: () -> Unit,
    isRoundTrip: Boolean,
    createdAt: String?,
    description: String,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RouteDetailStatCard("→ Distance", "%.1f km".format(distanceKm), Modifier.weight(1f))
                RouteDetailStatCard("↑ Elev. Gain", "%.0f m".format(elevGainM), Modifier.weight(1f))
                RouteDetailStatCard("↓ Elev. Loss", "%.0f m".format(elevLossM), Modifier.weight(1f))
            }
        }

        if (elevationPoints.isNotEmpty()) {
            item {
                Column {
                    Text(
                        "Elevation Profile",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    ElevationProfile(
                        points = elevationPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    if (createdAt != null) {
                        RouteDetailsMetaRow(
                            sportType = sportType,
                            isRoundTrip = isRoundTrip,
                            createdAt = createdAt,
                        )
                    } else {
                        RouteMetaRow(
                            sportType = sportType,
                            isRoundTrip = isRoundTrip,
                            onSportTypeChange = onSportTypeChange,
                        )
                    }

                    if (description.isNotBlank()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.4f
                            )
                        )
                        Text(
                            description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}
