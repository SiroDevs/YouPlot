/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.you.plot.feature.startpoint.form.view

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.you.plot.core.ui.components.action.AppTopBar
import com.you.plot.core.ui.components.maps.LocationSearchBar
import com.you.plot.core.ui.components.maps.PlotterMap
import com.you.plot.feature.startpoint.form.viewmodel.StartPointFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPointFormScreen(
    viewModel: StartPointFormViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(state.needsLocationPermission) {
        if (state.needsLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
            viewModel.clearNeedsPermission()
        }
    }

    LaunchedEffect(state.savedId) { state.savedId?.let(onSaved) }

    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Notice") },
            text = { Text(err) },
            confirmButton = { TextButton(onClick = viewModel::clearError) { Text("OK") } },
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (state.id == 0L) "New Start Point" else "Edit Start Point",
                showGoBack = true,
                onNavIconClick = onBack,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::save,
                icon = {
                    if (state.isSaving) CircularProgressIndicator(Modifier.padding(2.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Save, contentDescription = null)
                },
                text = { Text(if (state.isSaving) "Saving..." else "Save") },
                expanded = state.position != null && state.name.isNotBlank() && !state.isSaving,
            )
        },
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)) {
            PlotterMap(
                modifier = Modifier.fillMaxSize(),
                startPoint = state.position,
                endPoint = null,
                waypoints = emptyList(),
                candidates = emptyList(),
                selectedCandidateId = null,
                isRoundTrip = false,
                startPointName = state.name,
                endPointName = "",
                onMapTap = viewModel::onMapTap,
            )
            Column(Modifier.fillMaxWidth()) {
                LocationSearchBar(
                    query = state.query,
                    onQryChange = viewModel::onQryClear,
                    onSearch = viewModel::onSearch,
                    results = state.results,
                    isSearching = state.isSearching,
                    placeholder = "Search for a starting spot...",
                    onResultSelected = viewModel::onResultSelected,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    selectedCtryCode = state.countryCode,
                    onCountrySelected = viewModel::setCountryCode,
                    onChooseOnMap = viewModel::onQryClear,
                    onUseMyLocation = viewModel::onUseMyLocation,
                )
                if (state.position != null) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::setName,
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}
