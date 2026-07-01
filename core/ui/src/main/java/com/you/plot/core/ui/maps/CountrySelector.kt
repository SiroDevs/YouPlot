package com.you.plot.core.ui.maps

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.you.plot.core.common.utils.COUNTRY_LIST
import com.you.plot.core.common.utils.countryFlag

/**
 * Trigger that shows just the country's flag (no ISO letters — the ISO code still
 * flows through the caller via [selectedCtryCode]) and opens a full-page
 * [CountryPickerDialog] so users can search through the full list.
 */
@Composable
fun CountrySelector(
    selectedCtryCode: String,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }
    val flag = if (selectedCtryCode.isBlank()) "🌐" else countryFlag(selectedCtryCode)

    Box(modifier = modifier) {
        TextButton(
            onClick = { showPicker = true },
            modifier = Modifier.padding(end = 4.dp),
        ) {
            Text(flag, style = MaterialTheme.typography.titleMedium)
        }
    }

    if (showPicker) {
        CountryPickerDialog(
            selectedCtryCode = selectedCtryCode,
            onDismiss = { showPicker = false },
            onCountrySelected = {
                onCountrySelected(it)
                showPicker = false
            },
        )
    }
}

/** Full-screen searchable list of countries showing each country's flag + name. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerDialog(
    selectedCtryCode: String,
    onCountrySelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            var query by remember { mutableStateOf("") }
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }

            val filtered = remember(query) {
                if (query.isBlank()) COUNTRY_LIST
                else COUNTRY_LIST.filter { (code, name) ->
                    name.contains(query, ignoreCase = true) ||
                        code.equals(query.trim(), ignoreCase = true)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Country") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    )
                },
            ) { padding ->
                Column(Modifier
                    .fillMaxSize()
                    .padding(padding)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search country") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .focusRequester(focusRequester),
                    )

                    LazyColumn(Modifier.fillMaxSize()) {
                        items(filtered, key = { it.first.ifEmpty { "__all__" } }) { (code, name) ->
                            CountryRow(
                                code = code,
                                name = name,
                                isSelected = code == selectedCtryCode,
                                onClick = { onCountrySelected(code) },
                            )
                            HorizontalDivider(
                                Modifier.padding(start = 60.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            )
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryRow(
    code: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.surface
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (code.isBlank()) "🌍" else countryFlag(code),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.width(14.dp))
        Text(
            name.removePrefix("🌍 "),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun QuickActionRow(icon: @Composable () -> Unit, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        icon()
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
