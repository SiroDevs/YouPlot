package com.sirofits.youplot.tracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import com.sirofits.youplot.domain.repository.LocationRepository
import com.sirofits.youplot.domain.usecase.tracker.GetActiveSessionUseCase
import com.sirofits.youplot.domain.usecase.tracker.UpdateSessionLocationUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

/**
 * Foreground service that keeps GPS tracking alive while the app is backgrounded.
 * Mirrors a music player notification pattern — always visible, quick controls.
 */
@AndroidEntryPoint
class TrackingService : Service() {

    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var updateLocationUseCase: UpdateSessionLocationUseCase
    @Inject lateinit var getActiveSessionUseCase: GetActiveSessionUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var elapsedSeconds = 0L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    private fun start() {
        startForeground(NOTIFICATION_ID, buildNotification("Tracking active…", "0:00"))

        serviceScope.launch {
            locationRepository.getLocationUpdates(intervalMs = 5_000L)
                .catch { /* log */ }
                .collect { location ->
                    elapsedSeconds += 5
                    val session = getActiveSessionUseCase() ?: return@collect
                    updateLocationUseCase(
                        sessionId = session.id,
                        newLocation = location,
                        speedKmh = 0.0,
                        elapsedSeconds = elapsedSeconds,
                    )
                    updateNotification(session.distanceCoveredKm, elapsedSeconds)
                }
        }
    }

    private fun pause() {
        serviceScope.coroutineContext.cancelChildren()
        updateNotification(distanceKm = 0.0, elapsed = elapsedSeconds, paused = true)
    }

    private fun stop() {
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(
        distanceKm: Double,
        elapsed: Long,
        paused: Boolean = false,
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val status = if (paused) "Paused" else "%.1f km · %s".format(
            distanceKm,
            formatElapsed(elapsed)
        )
        manager.notify(NOTIFICATION_ID, buildNotification(
            title = if (paused) "Activity paused" else "Activity in progress",
            status = status,
        ))
    }

    private fun buildNotification(title: String, status: String): Notification {
        ensureChannel()

        // Pause / Resume action
        val pauseIntent = PendingIntent.getService(
            this, 0,
            Intent(this, TrackingService::class.java).apply { action = ACTION_PAUSE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, TrackingService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pauseIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Activity Tracking",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply { description = "Live activity tracking progress" }
            )
        }
    }

    private fun formatElapsed(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%d:%02d".format(m, s)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "com.sirofits.youplot.tracker.START"
        const val ACTION_PAUSE = "com.sirofits.youplot.tracker.PAUSE"
        const val ACTION_STOP = "com.sirofits.youplot.tracker.STOP"
        private const val CHANNEL_ID = "youplot_tracking"
        private const val NOTIFICATION_ID = 1001

        fun startIntent(context: Context) =
            Intent(context, TrackingService::class.java).apply { action = ACTION_START }

        fun pauseIntent(context: Context) =
            Intent(context, TrackingService::class.java).apply { action = ACTION_PAUSE }

        fun stopIntent(context: Context) =
            Intent(context, TrackingService::class.java).apply { action = ACTION_STOP }
    }
}
