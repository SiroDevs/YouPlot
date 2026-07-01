package com.you.plot.feature.route.detail.view.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.you.plot.core.common.entity.SportType
import com.you.plot.core.ui.components.general.displayLabel

@Composable
fun RouteMetaRow(
    sportType: SportType,
    isRoundTrip: Boolean,
    createdAt: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Sport", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(sportType.displayLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
        }

        VerticalDivider(
            modifier = Modifier.height(25.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text("Route Type", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (isRoundTrip) "Round Trip" else "One-Way",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
        }

        if (createdAt != null) {
            VerticalDivider(
                modifier = Modifier.height(25.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Created", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(createdAt,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}

