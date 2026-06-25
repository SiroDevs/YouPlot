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

package com.you.plot.feature.plan.details.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.PlanEvent
import com.you.plot.feature.plan.details.view.screen.timeFmt
import java.util.Date

@Composable
fun DayTimeline(events: List<PlanEvent>, modifier: Modifier = Modifier) {
    if (events.isEmpty()) return

    val scrollState = rememberScrollState()
    val minTime = events.minOf { it.plannedTimeMillis }
    val maxTime = events.maxOf { it.plannedTimeMillis }.coerceAtLeast(minTime + 1)

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Box(modifier.horizontalScroll(scrollState)) {
        Row(
            Modifier
                .width(900.dp)
                .height(88.dp)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            events.forEachIndexed { index, event ->
                // Node + label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(min = 64.dp, max = 96.dp),
                ) {
                    Text(
                        timeFmt.format(Date(event.plannedTimeMillis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurface.copy(alpha = 0.65f),
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        Modifier.size(10.dp).clip(CircleShape).background(primaryColor),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        event.name.take(14),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (event.distanceCoveredKm > 0) {
                        Text(
                            "${"%.1f".format(event.distanceCoveredKm)} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurface.copy(alpha = 0.5f),
                        )
                    }
                }

                // Connector
                if (index < events.lastIndex) {
                    Box(
                        Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(primaryColor.copy(alpha = 0.35f)),
                    )
                }
            }
        }
    }
}
