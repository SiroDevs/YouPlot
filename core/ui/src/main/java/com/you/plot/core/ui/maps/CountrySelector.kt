package com.you.plot.core.ui.maps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.utils.COUNTRY_LIST
import kotlin.text.ifEmpty

@Composable
fun CountrySelector(
    selectedCtryCode: String,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCountryMenu by remember { mutableStateOf(false) }

    val countryLabel = COUNTRY_LIST
        .firstOrNull { it.first == selectedCtryCode }
        ?.first
        ?.uppercase()
        ?.ifEmpty { "ALL" }
        ?: "KE"

    Box(modifier = modifier) {
        TextButton(
            onClick = { showCountryMenu = true },
            modifier = Modifier.padding(end = 4.dp),
        ) {
            Text(
                countryLabel.ifEmpty { "🌍" },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Select country",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }

        DropdownMenu(
            expanded = showCountryMenu,
            onDismissRequest = { showCountryMenu = false },
        ) {
            COUNTRY_LIST.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                code.uppercase().ifEmpty { "🌍" },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    onClick = {
                        onCountrySelected(code)
                        showCountryMenu = false
                    },
                )
            }
        }
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
