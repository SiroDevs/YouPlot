package com.you.plot.feature.route.plotter.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.feature.route.list.viewmodel.RouteCandidate

@Composable
fun RouteCandidateCard(
    candidate: RouteCandidate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val dotColor = Color(candidate.colorArgb or 0xFF000000L)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Colour dot
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )

            Column(Modifier.weight(1f)) {
                Text("Route ${candidate.id + 1}", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${"%.1f".format(candidate.totalDistanceKm)} km",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "↑ ${"%.0f".format(candidate.totalElevationGainMeters)} m",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "↓ ${"%.0f".format(candidate.totalElevationLossMeters)} m",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isSelected) {
                Icon(Icons.Default.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun RouteTypeCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) Icon(
                Icons.Default.Check,
                "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}