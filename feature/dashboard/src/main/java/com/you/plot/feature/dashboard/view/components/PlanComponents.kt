package com.you.plot.feature.dashboard.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.feature.dashboard.dashboard.utils.PlanFilter
import com.you.plot.feature.dashboard.view.screen.dateFmt
import java.util.Date

@Composable
fun PlanFilterRow(selected: PlanFilter, onSelect: (PlanFilter) -> Unit) {
    Row(
        Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlanFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                leadingIcon = {
                    Icon(
                        when (filter) {
                            PlanFilter.ALL -> Icons.AutoMirrored.Filled.List
                            PlanFilter.UPCOMING -> Icons.Default.Schedule
                            PlanFilter.PAST -> Icons.Default.History
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

@Composable
fun DashboardPlanItem(
    plan: ActivityPlan,
    sportType: SportType?,
    onClick: () -> Unit,
    onStartTracking: () -> Unit,
) {
    val isPast = plan.startDate < System.currentTimeMillis()
    val contentAlpha = if (isPast) 0.6f else 1f

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp).alpha(contentAlpha),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon badge — reflects the plan's sport type, falls back to a
            // calendar icon if the underlying route can't be resolved (e.g. deleted)
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.size(44.dp),
            ) {
                Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        sportType?.let { sportTypeIcon(it) } ?: Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(Modifier.size(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        plan.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (sportType != null) {
                        Spacer(Modifier.width(6.dp))
                        SportTypeChip(sportType)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    "${plan.numberOfDays} day${if (plan.numberOfDays > 1) "s" else ""} · ${"%.1f".format(plan.avgDailyDist)} km/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    if (isPast) "Completed ${dateFmt.format(Date(plan.startDate))}"
                    else "Starts ${dateFmt.format(Date(plan.startDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!isPast) {
                IconButton(onClick = onStartTracking) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start tracking",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}
