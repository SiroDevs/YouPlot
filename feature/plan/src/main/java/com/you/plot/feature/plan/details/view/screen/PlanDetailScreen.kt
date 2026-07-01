package com.you.plot.feature.plan.details.view.screen

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.dateFmt
import com.you.plot.core.data.export.PlanExportFormat
import com.you.plot.core.domain.entity.ActivityPlan
import com.you.plot.core.ui.action.AppTopBar
import com.you.plot.core.ui.dialog.ExportChoice
import com.you.plot.core.ui.dialog.ExportFormatSheet
import com.you.plot.feature.plan.details.view.components.AddReminderDialog
import com.you.plot.feature.plan.details.view.components.DaySummaryCard
import com.you.plot.core.ui.general.DayTimeline
import com.you.plot.feature.plan.details.view.components.EventCard
import com.you.plot.feature.plan.details.view.components.ReminderRow
import com.you.plot.feature.plan.details.viewmodel.PlanDetailViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(
    viewModel: PlanDetailViewModel,
    onBack: () -> Unit,
    onStartTracking: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showExportSheet by remember { mutableStateOf(false) }

    if (state.showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { viewModel.hideAddReminderDialog() },
            onConfirm = { label, fireAt -> viewModel.addReminder(context, label, fireAt) },
        )
    }

    LaunchedEffect(state.pendingShareUri) {
        val uri = state.pendingShareUri ?: return@LaunchedEffect
        val mime = state.pendingShareMime ?: "*/*"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Export plan"))
        viewModel.consumeShare()
    }

    state.exportError?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.clearExportError() },
            title = { Text("Export failed") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearExportError() }) { Text("OK") }
            },
        )
    }

    if (showExportSheet) {
        ExportFormatSheet(
            title = "Export plan as",
            formats = PlanExportFormat.entries.map { format ->
                ExportChoice(
                    label = format.display,
                    description = when (format) {
                        PlanExportFormat.PDF -> "Printable itinerary"
                        PlanExportFormat.TXT -> "Plain text summary"
                        PlanExportFormat.IMAGE -> "Screenshot — coming soon"
                    },
                    enabled = format != PlanExportFormat.IMAGE,
                    onSelect = {
                        showExportSheet = false
                        viewModel.exportPlan(format)
                    },
                )
            },
            onDismiss = { showExportSheet = false },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = state.plan?.name ?: "Plan",
                onNavIconClick = onBack,
                actions = {
                    state.plan?.let { plan ->
                        IconButton(onClick = {
                            val intent = buildCalendarIntent(plan)
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.DateRange, "Export to Calendar")
                        }
                    }
                    IconButton(onClick = { showExportSheet = true }) {
                        Icon(Icons.Outlined.IosShare, "Export plan")
                    }
                    IconButton(onClick = { viewModel.showAddReminderDialog() }) {
                        Icon(Icons.Default.Notifications, "Add Reminder")
                    }
                    state.plan?.let { plan ->
                        IconButton(onClick = { onStartTracking(plan.id) }) {
                            Icon(Icons.Default.PlayArrow, "Start tracking")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val plan = state.plan ?: return@Scaffold

        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "${plan.numberOfDays} day(s) · ${"%.1f".format(plan.avgDailyDist)} km/day · ${"%.0f".format(plan.avgSpeed)} km/h",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    "Starts ${dateFmt.format(Date(plan.startDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (plan.description.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(plan.description, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(plan.numberOfDays) { i ->
                        val day = i + 1
                        FilterChip(
                            selected = state.selectedDay == day,
                            onClick = { viewModel.selectDay(day) },
                            label = { Text("Day $day") },
                        )
                    }
                }
            }

            item {
                DayTimeline(
                    events = state.eventsForDay,
                    modifier = Modifier.fillMaxWidth().height(88.dp),
                )
                Spacer(Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
            }

            items(state.eventsForDay, key = { it.id }) { event ->
                EventCard(event = event)
            }

            item {
                Spacer(Modifier.height(4.dp))
                DaySummaryCard(state = state)
            }

            if (state.reminders.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Reminders", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                }
                items(state.reminders, key = { it.index }) { reminder ->
                    ReminderRow(
                        entry = reminder,
                        onRemove = { viewModel.removeReminder(context, reminder) },
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

private fun buildCalendarIntent(plan: ActivityPlan): Intent {
    val endMillis = plan.startDate + plan.numberOfDays * 86_400_000L
    return Intent(Intent.ACTION_INSERT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, plan.name)
        putExtra(CalendarContract.Events.DESCRIPTION,
            plan.description.ifBlank { "${"%.1f".format(plan.avgDailyDist)} km/day · ${"%.0f".format(plan.avgSpeed)} km/h" })
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, plan.startDate)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        putExtra(CalendarContract.Events.ALL_DAY, plan.numberOfDays > 1)
    }
}