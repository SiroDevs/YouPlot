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

package com.you.plot.feature.tracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.you.plot.core.common.utils.NotifConstants
import com.you.plot.core.domain.repos.LocationRepo
import com.you.plot.core.domain.usecase.tracker.CompleteSessionUseCase
import com.you.plot.core.domain.usecase.tracker.GetActiveSessionUseCase
import com.you.plot.core.domain.usecase.tracker.UpdateSessionLocationUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {

    @Inject lateinit var locationRepository: LocationRepo
    @Inject lateinit var updateLocationUseCase: UpdateSessionLocationUseCase
    @Inject lateinit var getActiveSessionUseCase: GetActiveSessionUseCase
    @Inject lateinit var completeSessionUseCase: CompleteSessionUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackingJob: Job? = null
    private var elapsedSeconds = 0L

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP  = "ACTION_STOP"
        const val EXTRA_SESSION_ID = "session_id"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
                if (sessionId != -1L) startTracking(sessionId)
            }
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking(sessionId: Long) {
        createNotificationChannel()
        val notification = buildNotification("Activity in progress…")
        startForeground(NotifConstants.NOTIF_TRACKING_ID, notification)

        trackingJob = serviceScope.launch {
            locationRepository.getLocationUpdates(intervalMs = 5_000L).collect { location ->
                elapsedSeconds += 5
                updateLocationUseCase(
                    sessionId = sessionId,
                    newLocation = location,
                    speedKmh = 0.0, // FusedLocation provides speed via Location.speed if needed
                    elapsedSeconds = elapsedSeconds,
                )
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        serviceScope.launch {
            getActiveSessionUseCase()?.let { completeSessionUseCase(it.id) }
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotifConstants.CHANNEL_TRACKING_ID,
            NotifConstants.CHANNEL_TRACKING_NAME,
            NotificationManager.IMPORTANCE_LOW,
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(contentText: String) =
        NotificationCompat.Builder(this, NotifConstants.CHANNEL_TRACKING_ID)
            .setContentTitle("YouPlot")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
