package com.you.plot.feature.plan.list.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import com.you.plot.core.domain.entity.ActivityPlan
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun PlanItem(plan: ActivityPlan, onClick: () -> Unit, onStartTracking: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(plan.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${plan.numberOfDays} day(s) · %.1f km/day".format(plan.avgDistancePerDayKm),
                    style = MaterialTheme.typography.bodySmall,
                )
                val dateStr = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale)
                    .format(Date(plan.startDateMillis))
                Text("Starts $dateStr", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onStartTracking) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start tracking")
            }
        }
    }
}