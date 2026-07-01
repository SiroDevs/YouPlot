package com.you.plot.feature.plan.list.view.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlanItem(
    plan: ActivityPlan,
    menuOpen: Boolean,
    onClick: () -> Unit,
    onStartTracking: () -> Unit,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .combinedClickable(onClick = onClick, onLongClick = onOpenMenu),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(plan.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${plan.numberOfDays} day(s) · %.1f km/day".format(plan.avgDailyDist),
                    style = MaterialTheme.typography.bodySmall,
                )
                val dateStr = SimpleDateFormat("dd MMM yyyy", LocalLocale.current.platformLocale)
                    .format(Date(plan.startDate))
                Text("Starts $dateStr", style = MaterialTheme.typography.bodySmall)
            }
            if (plan.isFavorite) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Box {
                IconButton(onClick = onStartTracking) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start tracking")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = onDismissMenu) {
                    DropdownMenuItem(
                        text = { Text(if (plan.isFavorite) "Unfavorite" else "Favorite") },
                        leadingIcon = {
                            Icon(
                                if (plan.isFavorite) Icons.Outlined.Star
                                else Icons.Outlined.StarOutline,
                                null,
                            )
                        },
                        onClick = { onDismissMenu(); onToggleFavorite() },
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate & adjust") },
                        leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) },
                        onClick = { onDismissMenu(); onDuplicate() },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = { onDismissMenu(); onDelete() },
                    )
                }
            }
        }
    }
}
