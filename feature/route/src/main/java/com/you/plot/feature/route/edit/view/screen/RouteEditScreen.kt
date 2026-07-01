package com.you.plot.feature.route.edit.view.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.ui.action.AppTopBar
import com.you.plot.core.ui.dialog.PickerDialog
import com.you.plot.core.ui.general.displayLabel
import com.you.plot.core.ui.general.icon
import com.you.plot.feature.route.edit.viewmodel.RouteEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteEditScreen(
    viewModel: RouteEditViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var showSportPicker by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) onBack()
    }

    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Cannot save") },
            text = { Text(err) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("OK") }
            },
        )
    }

    if (showSportPicker) {
        PickerDialog(
            title = "Sport Type",
            options = SportType.entries.map { it to it.displayLabel },
            selected = state.sportType,
            onDismiss = { showSportPicker = false },
            onConfirm = { viewModel.setSportType(it); showSportPicker = false },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Edit Route",
                showGoBack = true,
                onNavIconClick = onBack,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::save,
                icon = {
                    if (state.isSaving) CircularProgressIndicator(
                        Modifier.padding(2.dp), strokeWidth = 2.dp,
                    )
                    else Icon(Icons.Default.Save, null)
                },
                text = { Text(if (state.isSaving) "Saving..." else "Save") },
                expanded = state.hasChanges && !state.isSaving,
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        if (state.original == null) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Text("Route not found")
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text("Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::setDescription,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSportPicker = true },
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        state.sportType.icon,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Sport Type",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            state.sportType.displayLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(
                        "Change",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Default.Loop,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Round trip",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Distance & elevation recalculate when this changes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.isRoundTrip,
                        onCheckedChange = viewModel::setRoundTrip,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "To change the start point, destination, or waypoints, plot a new route.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(88.dp))
        }
    }
}
