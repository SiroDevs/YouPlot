package com.you.plot.feature.tracker.view.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.ActivityActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ConstantLocale")
private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

/**
 * Two rows of [StatCard]s covering distance/speed/elapsed on top and next-ETA /
 * done-ETA / status on the bottom.
 */
@Composable
fun TrackerStatsGrid(
    activity: ActivityActivity,
    nextEta: Long?,
    doneEta: Long?,
    modifier: Modifier = Modifier,
) {
    val elapsed = activity.elapsedTime
    val h = elapsed / 3600
    val m = (elapsed % 3600) / 60
    val s = elapsed % 60

    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                "Distance",
                "%.2f km".format(activity.distCovered),
                Modifier.weight(1f),
            )
            StatCard(
                "Speed",
                "%.1f km/h".format(activity.currentSpeed),
                Modifier.weight(1f),
            )
            StatCard(
                "Elapsed",
                "%02d:%02d:%02d".format(h, m, s),
                Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                "Next WP ETA",
                nextEta?.let { timeFmt.format(Date(it)) } ?: "—",
                Modifier.weight(1f),
            )
            StatCard(
                "Done ETA",
                doneEta?.let { timeFmt.format(Date(it)) } ?: "—",
                Modifier.weight(1f),
            )
            StatCard(
                "Status",
                activity.status.name.lowercase().replaceFirstChar { it.uppercase() },
                Modifier.weight(1f),
            )
        }
    }
}
