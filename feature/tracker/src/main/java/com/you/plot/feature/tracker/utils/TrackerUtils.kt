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

package com.you.plot.feature.tracker.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.you.plot.core.domain.entity.ActivityActivity
import com.you.plot.core.domain.entity.WaypointProgress

data class TrackerUiState(
    val activity: ActivityActivity? = null,
    val isLoading: Boolean = true,
    // Permission flow (spec §3.1)
    val locationPermissionGranted: Boolean = false,
    val backgroundLocationGranted: Boolean = false,
    val activityRecognitionGranted: Boolean = false,
    val locationServicesEnabled: Boolean = false,
    val showPermissionRationale: Boolean = false,
    // Stop reminder (spec §3.4) — full-screen flag
    val pendingStopWaypoint: WaypointProgress? = null,
    val showFullScreenStopReminder: Boolean = false,
    // Error
    val error: String? = null,
) {
    val allPermissionsGranted: Boolean
        get() = locationPermissionGranted && backgroundLocationGranted && locationServicesEnabled

    // Convenience shorthands for the UI
    val nextUnreachedWaypoint: WaypointProgress?
        get() = activity?.waypointProgress?.firstOrNull { !it.isReached && !it.wasSkipped }

    val estimatedCompletion: Long?
        get() = activity?.estimatedCompletion
}

fun vibrate(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vm.defaultVibrator.vibrate(
            VibrationEffect.createOneShot(600L, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    } else {
        @Suppress("DEPRECATION")
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        @Suppress("DEPRECATION")
        v.vibrate(600L)
    }
}
