package com.you.plot.feature.tracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.you.plot.core.common.utils.NotifConstants
import com.you.plot.core.common.entity.LatLng
import com.you.plot.core.domain.repos.LocationRepo
import com.you.plot.core.domain.usecase.tracker.CompleteSessionUseCase
import com.you.plot.core.domain.usecase.tracker.GetActiveSessionUseCase
import com.you.plot.core.domain.usecase.tracker.PauseSessionUseCase
import com.you.plot.core.domain.usecase.tracker.ResumeSessionUseCase
import com.you.plot.core.domain.usecase.tracker.UpdateSessionLocationUseCase
import com.you.plot.core.domain.usecase.tracker.distanceTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {

    @Inject lateinit var locationRepository: LocationRepo
    @Inject lateinit var updateLocationUseCase: UpdateSessionLocationUseCase
    @Inject lateinit var getActiveSessionUseCase: GetActiveSessionUseCase
    @Inject lateinit var pauseSessionUseCase: PauseSessionUseCase
    @Inject lateinit var resumeSessionUseCase: ResumeSessionUseCase
    @Inject lateinit var completeSessionUseCase: CompleteSessionUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var trackingJob: Job? = null
    private var elapsedSeconds = 0L
    private var prevLocation: LatLng? = null

    companion object {
        const val ACTION_START  = "ACTION_START"
        const val ACTION_STOP   = "ACTION_STOP"
        const val ACTION_PAUSE  = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val EXTRA_SESSION_ID = "session_id"

        // Notification action request codes
        private const val RC_PAUSE  = 10
        private const val RC_RESUME = 11
        private const val RC_STOP   = 12
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START  -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
                if (sessionId != -1L) startTracking(sessionId)
            }
            ACTION_PAUSE  -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP   -> stopTracking()
        }
        return START_STICKY
    }

    // ── Tracking loop ─────────────────────────────────────────────────────────

    private fun startTracking(sessionId: Long) {
        createNotificationChannel()
        startForeground(NotifConstants.NOTIF_TRACKING_ID, buildNotification(sessionId))

        trackingJob = serviceScope.launch {
            locationRepository.getLocationUpdates(intervalMs = 5_000L).collect { location ->
                elapsedSeconds += 5

                // Derive speed from distance delta over the 5-second tick
                val deltaDist = prevLocation?.distanceTo(location) ?: 0.0
                val speedKmh  = (deltaDist / (5.0 / 3600.0)).coerceAtMost(200.0)
                prevLocation  = location

                updateLocationUseCase(
                    sessionId      = sessionId,
                    newLocation    = location,
                    speedKmh       = speedKmh,
                    elapsedSeconds = elapsedSeconds,
                )

                // Refresh notification with live stats
                val session = getActiveSessionUseCase()
                if (session != null) {
                    val distStr  = "%.2f km".format(session.distanceCoveredKm)
                    val speedStr = "%.1f km/h".format(session.currentSpeedKmh)
                    val etaStr   = session.estimatedCompletionMillis
                        ?.let { "ETA ${timeFmt.format(Date(it))}" }
                        ?: ""
                    updateNotification(sessionId, "$distStr · $speedStr · $etaStr")
                }
            }
        }
    }

    // ── Pause / resume from notification ─────────────────────────────────────

    private fun handlePause() {
        trackingJob?.cancel()
        serviceScope.launch {
            getActiveSessionUseCase()?.let { pauseSessionUseCase(it.id) }
            updateNotification(-1L, "Paused")
        }
    }

    private fun handleResume() {
        serviceScope.launch {
            val session = getActiveSessionUseCase() ?: return@launch
            resumeSessionUseCase(session.id)
            startTracking(session.id)           // re-starts the collection loop
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

    // ── Notification (spec §3.6 – quick-access controls + live stats) ─────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NotifConstants.CHANNEL_TRACKING_ID,
            NotifConstants.CHANNEL_TRACKING_NAME,
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(sessionId: Long, contentText: String = "Activity in progress ...") =
        baseNotificationBuilder(contentText, sessionId).build()

    private fun updateNotification(sessionId: Long, contentText: String) {
        val notification = baseNotificationBuilder(contentText, sessionId).build()
        getSystemService(NotificationManager::class.java)
            .notify(NotifConstants.NOTIF_TRACKING_ID, notification)
    }

    private fun baseNotificationBuilder(contentText: String, sessionId: Long) =
        NotificationCompat.Builder(this, NotifConstants.CHANNEL_TRACKING_ID)
            .setContentTitle("YouPlot — Tracking")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setSilent(true)
            // Pause action
            .addAction(
                android.R.drawable.ic_media_pause,
                "Pause",
                buildServicePendingIntent(ACTION_PAUSE, RC_PAUSE),
            )
            // Resume action
            .addAction(
                android.R.drawable.ic_media_play,
                "Resume",
                buildServicePendingIntent(ACTION_RESUME, RC_RESUME),
            )
            // Stop action
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                buildServicePendingIntent(ACTION_STOP, RC_STOP),
            )

    private fun buildServicePendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, TrackingService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
}
