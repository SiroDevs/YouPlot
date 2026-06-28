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

package com.you.plot.feature.settings.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType
import com.you.plot.feature.settings.utils.SportSpeedLimits
import com.you.plot.feature.settings.view.screen.kmhToPaceStr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedLimitDialog(
    sport: SportType,
    limits: SportSpeedLimits,
    usePace: Boolean,
    onDismiss: () -> Unit,
    onSave: (Float, Float) -> Unit,
) {
    var range by remember { mutableStateOf(limits.minKmh..limits.maxKmh) }
    val absMax = when (sport) {
        SportType.RUNNING -> 35f
        SportType.CYCLING -> 100f
        SportType.HIKING -> 15f
        SportType.WALKING -> 15f
    }
    val absMin = 0.5f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "${
                    sport.name.lowercase().replaceFirstChar { it.uppercase() }
                } Speed Range"
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val minLabel =
                    if (usePace) kmhToPaceStr(range.start) else "%.1f km/h".format(range.start)
                val maxLabel =
                    if (usePace) kmhToPaceStr(range.endInclusive) else "%.1f km/h".format(range.endInclusive)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Min: $minLabel",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Max: $maxLabel",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                RangeSlider(
                    value = range,
                    onValueChange = { range = it },
                    valueRange = absMin..absMax,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (usePace) {
                    Text(
                        "Lower km/h = slower pace (higher min/km)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Miles equivalent
                val minMi = range.start * 0.621371f
                val maxMi = range.endInclusive * 0.621371f
                Text(
                    "≈ %.1f – %.1f mph".format(minMi, maxMi),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(range.start, range.endInclusive) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
