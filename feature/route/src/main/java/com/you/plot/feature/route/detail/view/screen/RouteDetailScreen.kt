package com.you.plot.feature.route.detail.view.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.you.plot.core.data.export.RouteExportFormat
import com.you.plot.core.ui.dialog.ExportChoice
import com.you.plot.core.ui.dialog.ExportFormatSheet
import com.you.plot.feature.route.detail.viewmodel.RouteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    viewModel: RouteDetailViewModel,
    onBack: () -> Unit,
    onCreatePlan: (Long) -> Unit,
    onEditRoute: (Long) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showExportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onBack() }

    LaunchedEffect(state.pendingShareUri) {
        val uri = state.pendingShareUri ?: return@LaunchedEffect
        val mime = state.pendingShareMime ?: "*/*"
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Export route"))
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
            title = "Export route as",
            formats = RouteExportFormat.entries.map { format ->
                val stubbed = format == RouteExportFormat.FIT || format == RouteExportFormat.IMAGE
                ExportChoice(
                    label = format.display,
                    description = when (format) {
                        RouteExportFormat.GPX -> "GPX 1.1 track + routepoints"
                        RouteExportFormat.TCX -> "Garmin Training Center course"
                        RouteExportFormat.PDF -> "Printable text summary"
                        RouteExportFormat.FIT -> "Binary FIT — coming soon"
                        RouteExportFormat.IMAGE -> "Map screenshot — coming soon"
                    },
                    enabled = !stubbed,
                    onSelect = {
                        showExportSheet = false
                        viewModel.exportRoute(format)
                    },
                )
            },
            onDismiss = { showExportSheet = false },
        )
    }

    state.deleteError?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteError() },
            title = { Text("Cannot delete route") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDeleteError() }) { Text("OK") }
            },
        )
    }

    RouteDetailContent(
        state = state,
        onBack = onBack,
        onCreatePlan = onCreatePlan,
        onDeleteRoute = viewModel::deleteRoute,
        onExportClick = { showExportSheet = true },
        onEditClick = { state.route?.let { onEditRoute(it.id) } },
        showMap = true,
    )
}
