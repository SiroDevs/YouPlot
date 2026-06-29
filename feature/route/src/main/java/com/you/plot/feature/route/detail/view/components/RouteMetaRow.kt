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

package com.you.plot.feature.route.detail.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.NordicWalking
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType

@Composable
fun RouteMetaRow(
    sportType: SportType,
    isRoundTrip: Boolean,
    createdAt: String?,
    editable: Boolean,
    onSportTypeChip: () -> Unit,
    onRoundTripChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Sport
        Column(modifier = Modifier.weight(1f)) {
            Icon(sportType.icon, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text("Sport", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(sportType.displayLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            if (editable) {
                Spacer(Modifier.height(8.dp))
                AssistChip(
                    onClick = onSportTypeChip,
                    label = { Text("Change", style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = {
                        Icon(Icons.Outlined.KeyboardArrowDown, null,
                            modifier = Modifier.size(14.dp))
                    },
                )
            }
        }

        VerticalDivider(
            modifier = Modifier.height(56.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        )

        // Route Type
        Column(modifier = Modifier.weight(1f)) {
            Icon(Icons.Outlined.Route, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text("Route Type", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (isRoundTrip) "Round Trip" else "One-Way",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            if (editable) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = !isRoundTrip,
                        onClick = { onRoundTripChange(false) },
                        label = { Text("One-Way", style = MaterialTheme.typography.labelSmall) },
                    )
                    FilterChip(
                        selected = isRoundTrip,
                        onClick = { onRoundTripChange(true) },
                        label = { Text("Loop", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = { Icon(Icons.Outlined.Loop, null, modifier = Modifier.size(12.dp)) },
                    )
                }
            }
        }

        // Created
        if (createdAt != null) {
            VerticalDivider(
                modifier = Modifier.height(56.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
            Column(modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.CalendarToday, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.height(4.dp))
                Text("Created", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}

val SportType.displayLabel: String
    get() = when (this) {
        SportType.RUNNING -> "Running"
        SportType.CYCLING -> "Cycling"
        SportType.HIKING  -> "Hiking"
        SportType.WALKING -> "Walking"
    }

val SportType.icon: ImageVector
    get() = when (this) {
        SportType.RUNNING -> Icons.Outlined.DirectionsRun
        SportType.CYCLING -> Icons.Outlined.DirectionsBike
        SportType.HIKING  -> Icons.Outlined.NordicWalking
        SportType.WALKING -> Icons.Outlined.DirectionsWalk
    }