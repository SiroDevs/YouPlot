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

package com.you.plot.core.ui.components.maps

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

@Composable
fun MapShimmer(modifier: Modifier = Modifier) {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surface

    val transition = rememberInfiniteTransition(label = "map_shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "map_shimmer_translate",
    )

    Box(
        modifier = modifier.background(baseColor),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            baseColor,
                            highlightColor.copy(alpha = 0.6f),
                            baseColor,
                        ),
                        start = Offset(translateAnim * 1000f - 400f, 0f),
                        end = Offset(translateAnim * 1000f, 400f),
                    )
                )
        )
    }
}