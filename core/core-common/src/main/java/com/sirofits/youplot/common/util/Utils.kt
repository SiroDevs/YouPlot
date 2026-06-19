package com.sirofits.youplot.common.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtils {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("EEE, dd MMM · HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

    fun formatTime(epochMs: Long): String = timeFormat.format(Date(epochMs))
    fun formatDateTime(epochMs: Long): String = dateTimeFormat.format(Date(epochMs))
    fun formatDate(epochMs: Long): String = dateFormat.format(Date(epochMs))

    fun formatElapsedTime(seconds: Long): String {
        val h = TimeUnit.SECONDS.toHours(seconds)
        val m = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s)
        else "%d:%02d".format(m, s)
    }

    fun formatDuration(minutes: Int): String = when {
        minutes < 60 -> "${minutes}m"
        minutes % 60 == 0 -> "${minutes / 60}h"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }

    fun etaLabel(plannedMs: Long, estimatedMs: Long): String {
        val diffMin = (estimatedMs - plannedMs) / 60_000
        return when {
            diffMin > 5 -> "${diffMin}m late"
            diffMin < -5 -> "${-diffMin}m early"
            else -> "On time"
        }
    }
}

object DistanceUtils {
    fun formatKm(km: Double): String = when {
        km < 1.0 -> "${(km * 1000).toInt()} m"
        else -> "%.1f km".format(km)
    }

    fun formatSpeed(kmh: Double): String = "%.1f km/h".format(kmh)
}
