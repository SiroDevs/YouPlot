package com.you.plot.feature.plan.details.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.timeFmt
import com.you.plot.core.domain.entity.Event
import com.you.plot.core.ui.general.SummaryRow
import com.you.plot.feature.plan.details.utils.PlanDetailUiState
import java.util.Date

@Composable
fun EventCard(event: Event) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Time pill
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    timeFmt.format(Date(event.plannedTime)),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Column(Modifier.weight(1f)) {
                Text(event.name, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "${"%.1f".format(event.distCovered)} km covered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (event.duration > 0) {
                        Text(
                            "${event.duration} min stop",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaySummaryCard(state: PlanDetailUiState) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                "Day ${state.selectedDay} Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            SummaryRow("Distance covered", "${"%.1f".format(state.dayTotalDist)} km")
            SummaryRow("Remaining distance", "${"%.1f".format(state.remainingDist)} km")
            val daysLeft = (state.plan?.numberOfDays ?: 1) - state.selectedDay
            if (daysLeft > 0) {
                SummaryRow(
                    "Adjusted target",
                    "${"%.1f".format(state.adjustedDailyDistanceKm)} km/day ($daysLeft day(s) left)",
                )
            }
        }
    }
}
